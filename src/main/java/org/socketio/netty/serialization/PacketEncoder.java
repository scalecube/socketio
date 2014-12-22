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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.socketio.netty.packets.Event;
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

	/**
	 * Don't let anyone instantiate this class.
	 */
	private PacketEncoder() {
	}

	public static String encodePacket(final Packet packet) throws IOException {
		StringBuilder result = new StringBuilder();
		result.append(packet.getType().getValue());
		result.append(DELIMITER);
		result.append(packet.getId());
		result.append(DELIMITER);
		result.append(packet.getEndpoint());
		if (packet.getData() != null) {
			result.append(DELIMITER);
			result.append(packet.getData());
		}
		return result.toString();
	}
}
