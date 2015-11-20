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
package io.scalecube.socketio.pipeline;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.activation.MimetypesFileTypeMap;

import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class ResourceHandler extends ChannelInboundHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 60;

	private final Map<String, URL> resources = new HashMap<String, URL>();

	public ResourceHandler() {
	}

	public void addResource(String pathPart, String resourcePath) {
		URL resUrl = getClass().getResource(resourcePath);
		if (resUrl == null) {
			log.error("The specified resource was not found: " + resourcePath);
			return;
		}
		resources.put(pathPart, resUrl);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			HttpRequest req = (HttpRequest) msg;
			QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
			String requestPath = queryDecoder.path();
			URL resUrl = resources.get(requestPath);
			if (resUrl != null) {
				if (log.isDebugEnabled())
					log.debug("Received HTTP resource request: {} {} from channel: {}", req.getMethod(), requestPath, ctx.channel());

				URLConnection fileUrl = resUrl.openConnection();
				long lastModified = fileUrl.getLastModified();
				// check if file has been modified since last request
				if (isNotModified(req, lastModified)) {
					sendNotModified(ctx);
					return;
				}
				// create resource input-stream and check existence
				final InputStream is = fileUrl.getInputStream();
				if (is == null) {
					sendError(ctx, HttpResponseStatus.NOT_FOUND);
					return;
				}
				// create ok response
				HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
				// set Content-Length header
				HttpHeaders.setContentLength(res, fileUrl.getContentLengthLong());
				// set Content-Type header
				setContentTypeHeader(res, fileUrl);
				// set Date, Expires, Cache-Control and Last-Modified headers
				setDateAndCacheHeaders(res, lastModified);
				// write initial response header
				ctx.write(res);

				// write the content stream
				ctx.pipeline().addBefore(ctx.name(), "chunked-writer-handler", new ChunkedWriteHandler());
				ChannelFuture writeFuture = ctx.writeAndFlush(new ChunkedStream(is, fileUrl.getContentLength()));
				// add operation complete listener so we can close the channel and the input stream
				writeFuture.addListener(ChannelFutureListener.CLOSE);
				ReferenceCountUtil.release(msg);
				return;
			}
		}
		super.channelRead(ctx, msg);
	}

	/*
	 * Checks if the content has been modified sicne the date provided by the IF_MODIFIED_SINCE http header
	 * */
	private boolean isNotModified(HttpRequest request, long lastModified) throws ParseException {
		String ifModifiedSince = request.headers().get(HttpHeaders.Names.IF_MODIFIED_SINCE);
		if (ifModifiedSince != null && !ifModifiedSince.equals("")) {
			SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
			Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

			// Only compare up to the second because the datetime format we send to the client does
			// not have milliseconds
			long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
			long fileLastModifiedSeconds = lastModified / 1000;
			return ifModifiedSinceDateSeconds == fileLastModifiedSeconds;
		}
		return false;
	}

	/*
	 * Sends a Not Modified response to the client
	 *
	 * */
	private void sendNotModified(ChannelHandlerContext ctx) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
		setDateHeader(response);

		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * Sets the Date header for the HTTP response
	 *
	 * @param response
	 *            HTTP response
	 */
	private void setDateHeader(HttpResponse response) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		Calendar time = new GregorianCalendar();
		HttpHeaders.setHeader(response, HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));
	}

	/**
	 * Sends an Error response with status message
	 *
	 * @param ctx channel context
	 * @param status status
	 */
	private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		ByteBuf content = Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8);

		ctx.write(response);
		// Close the connection as soon as the error message is sent.
		ctx.writeAndFlush(content).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * Sets the Date and Cache headers for the HTTP Response
	 *
	 * @param response
	 *            HTTP response
	 */
	private void setDateAndCacheHeaders(HttpResponse response, long lastModified) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
		dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

		// Date header
		Calendar time = new GregorianCalendar();
		HttpHeaders.setHeader(response, HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));

		// Add cache headers
		time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
		HttpHeaders.setHeader(response, HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));
		HttpHeaders.setHeader(response, HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
		HttpHeaders.setHeader(response, HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(lastModified)));
	}

	/**
	 * Sets the content type header for the HTTP Response
	 *
	 * @param response
	 *            HTTP response
	 */
	private void setContentTypeHeader(HttpResponse response, URLConnection resUrlConnection) {
		MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
		String resName = resUrlConnection.getURL().getFile();
		HttpHeaders.setHeader(response, HttpHeaders.Names.CONTENT_TYPE, mimeTypesMap.getContentType(resName));
	}
}
