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
import java.util.regex.Pattern;

import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;

public final class PacketDecoder {

	private static final Pattern PACKET_SPLIT_PATTERN = Pattern.compile("[:]");
	private static final int PACKET_TYPE_INDEX = 0;
	private static final int PACKET_MESSAGE_ID_INDEX = 1;
	private static final int PACKET_ENDPOINT_INDEX = 2;
	private static final int PACKET_DATA_INDEX = 3;

	/**
	 * Don't let anyone instantiate this class.
	 */
	private PacketDecoder() {
	}

	public static Packet decodePacket(final String msg) throws IOException {
		String[] messageTokens = PACKET_SPLIT_PATTERN.split(msg, 4);

		if (messageTokens.length < 3 || messageTokens.length > 4) {
			return Packet.NULL_INSTANCE;
		}

		// Resolve type
		int typeId = Integer.valueOf(messageTokens[PACKET_TYPE_INDEX]);
		PacketType type = PacketType.valueOf(typeId);

		// Resolve message id
		String messageId = messageTokens[PACKET_MESSAGE_ID_INDEX];

		// Resolve endpoint
		String endpoint = messageTokens[PACKET_ENDPOINT_INDEX];

		// Resolve data
		String data = "";
		if (messageTokens.length > PACKET_DATA_INDEX) {
			data = messageTokens[PACKET_DATA_INDEX];
		}

		// Create instance of packet
		Packet packet = new Packet(type);
		packet.setId(messageId);
		packet.setEndpoint(endpoint);
		packet.setData(data);

		return packet;
	}

}
