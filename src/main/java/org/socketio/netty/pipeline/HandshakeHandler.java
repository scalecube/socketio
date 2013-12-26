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

import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.serialization.JsonObjectMapperProvider;

/**
 * This class implements Socket.IO handshake procedure described below:
 * 
 * <h1>Handshake</h1>
 * 
 * <p>
 * The client will perform an initial HTTP POST request like the following
 * </p>
 * 
 * <p>
 * {@code http://example.com/socket.io/1/}
 * </p>
 * 
 * <p>
 * The absence of the transport id and session id segments will signal the
 * server this is a new, non-handshaken connection.
 * </p>
 * 
 * <p>
 * The server can respond in three different ways:
 * </p>
 * 
 * <p>
 * {@code 401 Unauthorized}
 * </p>
 * 
 * <p>
 * If the server refuses to authorize the client to connect, based on the
 * supplied information (eg: Cookie header or custom query components).
 * </p>
 * 
 * <p>
 * No response body is required.
 * </p>
 * 
 * <p>
 * {@code 503 Service Unavailable}
 * </p>
 * 
 * <p>
 * If the server refuses the connection for any reason (eg: overload).
 * </p>
 * 
 * <p>
 * No response body is required.
 * </p>
 * 
 * <p>
 * {@code 200 OK}
 * </p>
 * 
 * <p>
 * The handshake was successful.
 * </p>
 * 
 * <p>
 * The body of the response should contain the session id (sid) given to the
 * client, followed by the heartbeat timeout, the connection closing timeout,
 * and the list of supported transports separated by {@code :}
 * </p>
 * 
 * <p>
 * The absence of a heartbeat timeout ('') is interpreted as the server and
 * client not expecting heartbeats.
 * </p>
 * 
 * <p>
 * For example {@code 4d4f185e96a7b:15:10:websocket,xhr-polling}
 * </p>
 * 
 * @author Anton Kharenko, Ronen Hamias
 * 
 */
@Sharable
public class HandshakeHandler extends SimpleChannelUpstreamHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final String connectPath;
	private final String commonHandshakeParameters;

	public HandshakeHandler(final String connectPath, final int heartbeatTimeout, final int closeTimeout, final String transports) {
		this.connectPath = connectPath;
		commonHandshakeParameters = ":" + heartbeatTimeout + ":" + closeTimeout + ":" + transports;
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			final HttpRequest req = (HttpRequest) msg;
			final HttpMethod requestMethod = req.getMethod();
			final Channel channel = ctx.getChannel();
			final QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
			final String requestPath = queryDecoder.getPath();
			
			if (!requestPath.startsWith(connectPath)) {
				HttpResponse res = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
				ChannelFuture f = channel.write(res);
				f.addListener(ChannelFutureListener.CLOSE);
				return;
			}
			
			if (HttpMethod.GET.equals(requestMethod) && requestPath.equals(connectPath)) {
				log.debug("Received HTTP request: {} {} from channel: {}", new Object[] {
						requestMethod, requestPath, ctx.getChannel()});
				
				handshake(channel, req, queryDecoder);
				return;
			}
		}
		ctx.sendUpstream(e);
	}
	
	private void handshake(final Channel channel, final HttpRequest req, final QueryStringDecoder queryDecoder) throws IOException {
		final String sessionId = UUID.randomUUID().toString();
		if(log.isDebugEnabled()) log.debug("New sessionId: {} generated", sessionId);
		
		final String handshakeMessage = getHandshakeMessage(sessionId, queryDecoder);
		
		if(log.isDebugEnabled()) log.debug("Sending handshake: {} to channel: {}", handshakeMessage, channel);
		
		HttpResponse res = PipelineUtils.createHttpResponse(PipelineUtils.getOrigin(req), handshakeMessage, false);
		ChannelFuture f = channel.write(res);
		f.addListener(ChannelFutureListener.CLOSE);
	}
	
	private String getHandshakeMessage(final String sessionId, final QueryStringDecoder queryDecoder) throws IOException {
		String jsonpParam = extractJsonpParameter(queryDecoder);
		String handshakeParameters = sessionId + commonHandshakeParameters;
		if (jsonpParam != null) {            
			return "io.j[" + jsonpParam + "]("  
				+ JsonObjectMapperProvider.getObjectMapper().writeValueAsString(handshakeParameters) + ");";        
		} else {
			return handshakeParameters;
		}
	}
	
	private String extractJsonpParameter(QueryStringDecoder queryDecoder) {
		final Map<String, List<String>> params = queryDecoder.getParameters();
		List<String> jsonpParams = params.get("jsonp");
		return (jsonpParams != null) ? jsonpParams.get(0) : null;
	}
	
	

}
