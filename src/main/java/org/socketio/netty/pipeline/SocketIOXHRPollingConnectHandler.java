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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.ConnectPacket;

public class SocketIOXHRPollingConnectHandler extends SimpleChannelUpstreamHandler {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final String xhrPollingConnectPath;

	public SocketIOXHRPollingConnectHandler(final String connectPath) {
		this.xhrPollingConnectPath = connectPath + "xhr-polling";
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			final HttpRequest req = (HttpRequest) msg;
			final HttpMethod requestMethod = req.getMethod();
			final QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
			final String requestPath = queryDecoder.getPath();
			
			if (HttpMethod.GET.equals(requestMethod) && requestPath.startsWith(xhrPollingConnectPath)) {
				log.debug("Received HTTP request: {} {} from channel: {}", new Object[] {
						requestMethod, requestPath, ctx.getChannel()});
				
				final String sessionId = PipelineUtils.getSessionId(requestPath);
				final ConnectPacket packet = new ConnectPacket(sessionId, PipelineUtils.getOrigin(req));
				packet.setTransportType(TransportType.XHR_POLLING);
				Channels.fireMessageReceived(ctx, packet);
				return;
			}
		}
		ctx.sendUpstream(e);
	}

}
