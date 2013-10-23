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
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.serialization.PacketFramer;

/**
 * Certain transports, like websocket or flashsocket, have built-in lightweight
 * framing mechanisms for sending and receiving messages. For xhr-multipart, the
 * built-in MIME framing is used for the sake of consistency. When no built-in
 * lightweight framing is available, and multiple messages need to be delivered
 * (i.e: buffered messages), the following is used:
 * <p/>
 * {@code `\ufffd` [message lenth] `\ufffd`}
 * <p/>
 * Transports where the framing overhead is expensive (ie: when the xhr-polling
 * transport tries to send data to the server)
 * 
 * @author Ronen Hamias, Anton Kharenko
 */
public class SocketIOXHRPollingPacketDecoder extends OneToOneDecoder {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected Object decode(final ChannelHandlerContext ctx,
			final Channel channel, final Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			final HttpRequest req = (HttpRequest) msg;

			// Extract HTTP headers
			final QueryStringDecoder queryDecoder = new QueryStringDecoder(
					req.getUri());
			final String requestPath = queryDecoder.getPath();
			final HttpMethod requestMethod = req.getMethod();
			final String sessionId = PipelineUtils.getSessionId(requestPath);
			final String origin = PipelineUtils.getOrigin(req);

			log.debug("Received HTTP request: {} {} from channel: {}",
					new Object[] { requestMethod, requestPath, channel });

			if (req.getContent().hasArray()) {
				ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(req
						.getContent().array().length);
				buffer.writeBytes(req.getContent());

				int sequenceNumber = 0;
				while (buffer.readable()) {
					Packet packet = PacketFramer.decodeNextPacket(buffer);
					packet.setSessionId(sessionId);
					packet.setOrigin(origin);
					packet.setSequenceNumber(sequenceNumber);
					Channels.fireMessageReceived(channel, packet);
					sequenceNumber++;
				}
				return null;
			}
		}

		return msg;
	}
}
