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
package org.socketio.netty.serialization;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.util.CharsetUtil;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;

public final class PacketDecoder {

	private static final byte DELIMITER = (byte) ':';
	private static final ByteBufProcessor packetDelimiterFinder =  new ByteBufProcessor() {
		@Override
		public boolean process(byte value) throws Exception {
			return value != DELIMITER;
		}
	};

	/**
	 * Don't let anyone instantiate this class.
	 */
	private PacketDecoder() {
	}

	public static Packet decodePacket(final ByteBuf payload) throws IOException {
		int payloadSize = payload.readableBytes();

		// Decode packet type
		int typeDelimiterIndex = payload.forEachByte(packetDelimiterFinder);
		ByteBuf typeBytes = payload.slice(0, typeDelimiterIndex);
		String typeString = typeBytes.toString(CharsetUtil.UTF_8);
		int typeId = Integer.valueOf(typeString);
		PacketType type = PacketType.valueOf(typeId);

		// Skip message id
		int messageIdDelimiterIndex = payload.forEachByte(typeDelimiterIndex + 1, payloadSize - typeDelimiterIndex - 1, packetDelimiterFinder);

		// Skip endpoint
		int endpointDelimiterIndex = payload.forEachByte(messageIdDelimiterIndex + 1, payloadSize - messageIdDelimiterIndex - 1, packetDelimiterFinder);

		// Decode data
		String dataString;
		if (endpointDelimiterIndex != -1) {
			ByteBuf dataBytes = payload.slice(endpointDelimiterIndex + 1, payloadSize - endpointDelimiterIndex - 1);
			dataString =  dataBytes.toString(CharsetUtil.UTF_8);
		} else {
			dataString = "";
		}

		// Create instance of packet
		Packet packet = new Packet(type);
		packet.setData(dataString);

		return packet;
	}

}
