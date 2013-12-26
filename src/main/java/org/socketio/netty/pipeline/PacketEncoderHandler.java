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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.IPacket;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;
import org.socketio.netty.packets.PacketsFrame;
import org.socketio.netty.serialization.PacketEncoder;
import org.socketio.netty.serialization.PacketFramer;

/**
 * <p>
 * This class encodes Socket.IO messages they're sent. The structure of a
 * message is as follows:
 * </p>
 * 
 * <p>
 * {@code [message type] ':' [message id ('+')] ':' [message endpoint] (':' [message data])}
 * </p>
 * 
 * <p>
 * The message type is a single digit integer. The message id is an incremental
 * integer, required for ACKs (can be ommitted). If the message id is followed
 * by a +, the ACK is not handled by socket.io, but by the user instead.
 * Socket.IO has built-in support for multiple channels of communication (which
 * we call "multiple sockets"). Each socket is identified by an endpoint (can be
 * omitted).
 * </p>
 * 
 * @author Ronen Hamias, Anton Kharenko
 * 
 */
@Sharable
public class PacketEncoderHandler extends OneToOneEncoder {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected Object encode(final ChannelHandlerContext ctx, final Channel channel, final Object msg) throws Exception {
		if (msg instanceof IPacket) {
			IPacket packet = (IPacket) msg;

			log.debug("Sending packet: {} to channel: {}", msg, channel);
			CharSequence encodedPacket = encodePacket(packet);
			log.debug("Encoded packet: {}", encodedPacket);
			
			TransportType transportType = packet.getTransportType();
			if (transportType == TransportType.WEBSOCKET) {
				return new TextWebSocketFrame(encodedPacket.toString());
			} else if (transportType == TransportType.XHR_POLLING) {
				boolean json = packet.getType() == PacketType.JSON;
				return PipelineUtils.createHttpResponse(packet.getOrigin(), encodedPacket, json);
			} else {
				throw new UnsupportedTransportTypeException(transportType);
			}
		}
		return msg;
	}

	private CharSequence encodePacket(final IPacket msg) throws Exception {
		if (msg instanceof PacketsFrame) {
			return PacketFramer.encodePacketsFrame((PacketsFrame) msg);
		} else if (msg instanceof Packet) {
			return PacketEncoder.encodePacket((Packet) msg);
		} else {
			throw new UnsupportedPacketTypeException(msg);
		}
	}
}
