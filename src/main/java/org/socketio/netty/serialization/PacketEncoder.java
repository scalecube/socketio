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

import org.socketio.netty.packets.Packet;

/**
 * Class that provides encoding Socket.IO packets according to specification
 * below.
 * 
 * <h1>Encoding</h1>
 * <p/>
 * Messages have to be encoded before they're sent. The structure of a message
 * is as follows:
 * <p/>
 * {@code [message type] ':' [message id ('+')] ':' [message endpoint] (':' [message
 * data])}
 * <p/>
 * The message type is a single digit integer.
 * <p/>
 * The message id is an incremental integer, required for ACKs (can be
 * ommitted). If the message id is followed by a {@code +}, the ACK is not handled by
 * socket.io, but by the user instead.
 * <p/>
 * Socket.IO has built-in support for multiple channels of communication (which
 * we call "multiple sockets"). Each socket is identified by an endpoint (can be
 * omitted).
 * 
 * @author Anton Kharenko
 * 
 */
public final class PacketEncoder {

	private static final String DELIMITER = ":";
	private static final int DELIMITER_LENGTH = DELIMITER.length();

	private static final ThreadLocal<StringBuilder> reusableStringBuilder = new ThreadLocal<StringBuilder>() {
		@Override
		protected StringBuilder initialValue() {
			return new StringBuilder();
		}
	};

	/**
	 * Don't let anyone instantiate this class.
	 */
	private PacketEncoder() {
	}

	public static String encodePacket(final Packet packet) throws IOException {
		String type = packet.getType().getValueAsString();
		String messageId = packet.getId();
		String data = packet.getData();

		int capacity = type.length() + DELIMITER_LENGTH + messageId.length() + DELIMITER_LENGTH;
		if (data != null) {
			capacity += DELIMITER_LENGTH + data.length();
		}

		StringBuilder result = getReusableStringBuilder(capacity);
		result.ensureCapacity(capacity);
		result.append(type);
		result.append(DELIMITER);
		result.append(messageId);
		result.append(DELIMITER);
		if (data != null) {
			result.append(DELIMITER);
			result.append(data);
		}

		return result.toString();
	}

	private static StringBuilder getReusableStringBuilder(int capacity) {
		StringBuilder stringBuilder = reusableStringBuilder.get();
		stringBuilder.setLength(0);
		stringBuilder.ensureCapacity(capacity);
		return stringBuilder;
	}
}
