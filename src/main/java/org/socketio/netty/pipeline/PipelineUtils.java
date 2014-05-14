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

import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;

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
	private PipelineUtils() {
	}

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
		return req.headers().get(HttpHeaders.Names.ORIGIN);
	}

	public static String extractParameter(QueryStringDecoder queryDecoder, String key) {
		final Map<String, List<String>> params = queryDecoder.parameters();
		List<String> paramsByKey = params.get(key);
		return (paramsByKey != null) ? paramsByKey.get(0) : null;
	}

	public static HttpResponse createHttpResponse(final String origin, ByteBuf message, boolean json) {
		HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		if (json) {
			HttpHeaders.addHeader(res, HttpHeaders.Names.CONTENT_TYPE, "text/javascript; charset=UTF-8");
		} else {
			HttpHeaders.addHeader(res, HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
		}
		HttpHeaders.addHeader(res, HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		if (origin != null) {
			res.headers().add("Access-Control-Allow-Origin", origin);
			res.headers().add("Access-Control-Allow-Credentials", "true");
		}
		HttpHeaders.setContentLength(res, message.readableBytes());

		return res;
	}

}
