/**
 * Copyright 2012 Ronen Hamias, Anton Kharenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.socketio.netty.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.Cookie;
import org.jboss.netty.handler.codec.http.CookieDecoder;
import org.jboss.netty.handler.codec.http.CookieEncoder;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHttpRequestHandler extends OneToOneDecoder {
	
	private static final Logger log = LoggerFactory.getLogger(SimpleHttpRequestHandler.class);
	private static final String BASE_PATH = "/WEB-INF";
	
	private final String appPath;
	
	public SimpleHttpRequestHandler(final String basePath) {
		this.appPath = basePath;
	}

	@Override
	protected Object decode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest request = (HttpRequest) msg;
			QueryStringDecoder queryDecoder = new QueryStringDecoder(request.getUri());
			String path = queryDecoder.getPath();
			if (request.getUri().contains(appPath)) {
				StringBuilder content = getResourceContent(path);
				if (content != null) {
					writeResponse(ctx.getChannel(), request, content);
					return null;
				}
			}
		}
		return msg;
	}
	
	private StringBuilder getResourceContent(final String path) throws IOException {
		log.info("Requested Resource: {}", path);
		URL resourceFile = getClass().getResource(BASE_PATH + path);
		if (resourceFile == null) {
			return null;
		}
		InputStreamReader reader = new InputStreamReader(resourceFile.openStream());
		BufferedReader bufread = new BufferedReader(reader);
		String read;
		StringBuilder content = new StringBuilder();
		while ((read = bufread.readLine()) != null) {
			content.append(read + "\r\n");
		}
		return content;
	}
	
	public void writeResponse(final Channel channel, final HttpRequest request,final StringBuilder responseContent ) {
		// Convert the response content to a ChannelBuffer.
		ChannelBuffer buf = ChannelBuffers.copiedBuffer(responseContent, Charset.forName("UTF-8"));
		responseContent.setLength(0);
		
		// Decide whether to close the connection or not.
		final boolean close = isClose(request);

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response.setContent(buf);
		response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");

		if (!close) {
			// There's no need to add 'Content-Length' header
			// if this is the last response.
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(buf.readableBytes()));
		}

		String cookieString = request.getHeader(HttpHeaders.Names.COOKIE);
		if (cookieString != null) {
			CookieDecoder cookieDecoder = new CookieDecoder();
			Set<Cookie> cookies = cookieDecoder.decode(cookieString);
			if (!cookies.isEmpty()) {
				// Reset the cookies if necessary.
				CookieEncoder cookieEncoder = new CookieEncoder(true);
				for (Cookie cookie : cookies) {
					cookieEncoder.addCookie(cookie);
				}
				response.addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
			}
		}

		// Write the response.
		ChannelFuture future = channel.write(response);

		// Close the connection after the write operation is done if necessary.
		if (close) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	
	private boolean isClose(final HttpRequest request) {
		return HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) ||
			request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) &&
			!HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));
	}

}
