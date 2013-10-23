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
package org.socketio.netty.pipeline;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

/**
 * Utilities methods class. 
 * 
 * @author Anton Kharenko
 *
 */
final class PipelineUtils {
	
	/**
     * Don't let anyone instantiate this class.
     */
	private PipelineUtils() {}
	
	public static String getSessionId(final String requestPath) {
		String[] parts = requestPath.split("[/]");

		if (parts.length > 4 && !parts[4].isEmpty()) {
			String[] idsplit = parts[4].split("[?]"); 

			if (idsplit[0] != null && idsplit[0].length() > 0) {
				return idsplit[0];
			}

			return parts[4];
		}

		return null;
	}

	public static String getOrigin(final HttpRequest req) {
		return req.getHeader(HttpHeaders.Names.ORIGIN);
	}
	
	public static HttpResponse createHttpResponse(final String origin, CharSequence message, boolean json) {
		HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		
		// Add headers
		if (json) {
			res.addHeader(HttpHeaders.Names.CONTENT_TYPE, "application/javascript");
		} else {
			res.addHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		}
		res.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		if (origin != null) {
			res.addHeader("Access-Control-Allow-Origin", origin);
			res.addHeader("Access-Control-Allow-Credentials", "true");
		}
		
		// Add body
		res.setContent(ChannelBuffers.copiedBuffer(message, CharsetUtil.UTF_8));
		HttpHeaders.setContentLength(res, res.getContent().readableBytes());

		return res;
	}
	
	
}
