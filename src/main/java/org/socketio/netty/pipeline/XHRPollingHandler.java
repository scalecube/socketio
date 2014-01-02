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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.ConnectPacket;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.serialization.PacketFramer;

@Sharable
public class XHRPollingHandler extends SimpleChannelUpstreamHandler {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final String connectPath;

	public XHRPollingHandler(final String handshakePath) {
		this.connectPath = handshakePath + TransportType.XHR_POLLING.getName();
	}

	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			final HttpRequest req = (HttpRequest) msg;
			final HttpMethod requestMethod = req.getMethod();
			final QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
			final String requestPath = queryDecoder.getPath();
			
			if (requestPath.startsWith(connectPath)) {
				log.debug("Received HTTP XHR-Polling request: {} {} from channel: {}", new Object[] {
						requestMethod, requestPath, ctx.getChannel()});
				
				final String sessionId = PipelineUtils.getSessionId(requestPath);
				final String origin = PipelineUtils.getOrigin(req);
				
				if (HttpMethod.GET.equals(requestMethod)) {
					// Process polling request from client
					final ConnectPacket packet = new ConnectPacket(sessionId, origin);
					packet.setTransportType(TransportType.XHR_POLLING);
					Channels.fireMessageReceived(ctx, packet);
				} else if (HttpMethod.POST.equals(requestMethod)) {
					// Process message request from client
					if (req.getContent().hasArray()) {
						int contentLength = req.getContent().array().length;
						ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(contentLength);
						buffer.writeBytes(req.getContent());

						int sequenceNumber = 0;
						while (buffer.readable()) {
							Packet packet = PacketFramer.decodeNextPacket(buffer);
							packet.setSessionId(sessionId);
							packet.setOrigin(origin);
							packet.setSequenceNumber(sequenceNumber);
							Channels.fireMessageReceived(ctx.getChannel(), packet);
							sequenceNumber++;
						}
					}
				} else {
					log.warn("Can't process HTTP XHR-Polling request. Unknown request method: {} from channel: {}", requestMethod, ctx.getChannel());
				}
				return;
			}
		}
		ctx.sendUpstream(e);
	}

}
