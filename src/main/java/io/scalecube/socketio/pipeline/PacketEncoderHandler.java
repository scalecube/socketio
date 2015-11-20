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

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.scalecube.socketio.TransportType;
import io.scalecube.socketio.packets.Packet;
import io.scalecube.socketio.serialization.PacketEncoder;
import io.scalecube.socketio.serialization.PacketFramer;
import io.scalecube.socketio.packets.IPacket;
import io.scalecube.socketio.packets.PacketsFrame;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCounted;

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
@ChannelHandler.Sharable
public class PacketEncoderHandler extends MessageToMessageEncoder<Object> {
	
	private final static String JSONP_TEMPLATE = "io.j[%s]('%s');";
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
		if (msg instanceof IPacket) {
			IPacket packet = (IPacket) msg;

			if (log.isDebugEnabled())
				log.debug("Sending packet: {} to channel: {}", msg, ctx.channel());
			ByteBuf encodedPacket = encodePacket(packet);
			if (log.isDebugEnabled())
				log.debug("Encoded packet: {}", encodedPacket);

			TransportType transportType = packet.getTransportType();
			if (transportType == TransportType.WEBSOCKET || transportType == TransportType.FLASHSOCKET) {
				out.add(new TextWebSocketFrame(encodedPacket));
			} else if (transportType == TransportType.XHR_POLLING) {
				out.add(PipelineUtils.createHttpResponse(packet.getOrigin(), encodedPacket, false));
			} else if (transportType == TransportType.JSONP_POLLING) {
				String jsonpIndexParam = (packet.getJsonpIndexParam() != null) ? packet.getJsonpIndexParam() : "0";
				String encodedStringPacket = encodedPacket.toString(CharsetUtil.UTF_8);
				encodedPacket.release();
				String encodedJsonpPacket = String.format(JSONP_TEMPLATE, jsonpIndexParam, encodedStringPacket);
				HttpResponse httpResponse = PipelineUtils.createHttpResponse(packet.getOrigin(), PipelineUtils.copiedBuffer(ctx.alloc(), encodedJsonpPacket), true);
				httpResponse.headers().add("X-XSS-Protection", "0");
				out.add(httpResponse);
			} else {
				throw new UnsupportedTransportTypeException(transportType);
			}
		} else {
			if (msg instanceof ReferenceCounted) {
				((ReferenceCounted) msg).retain();
			}
			out.add(msg);
		}
	}

	private ByteBuf encodePacket(final IPacket msg) throws Exception {
		if (msg instanceof PacketsFrame) {
			return PacketFramer.encodePacketsFrame((PacketsFrame) msg);
		} else if (msg instanceof Packet) {
			return PacketEncoder.encodePacket((Packet) msg);
		} else {
			throw new UnsupportedPacketTypeException(msg);
		}
	}

}
