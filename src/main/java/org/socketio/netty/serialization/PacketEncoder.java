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

	/**
	 * Don't let anyone instantiate this class.
	 */
	private PacketEncoder() {
	}

	public static String encodePacket(final Packet packet) throws IOException {
		int type = packet.getType().getValue();
		String id = packet.getId();
		String endpoint = packet.getEndpoint();
		Object ack = packet.getAck();
		Object data = packet.getData();

		switch (packet.getType()) {
			case MESSAGE:
				if (packet.getData() != null) {
					data = packet.getData();
				}
				break;
	
			case EVENT:
				List<?> args = packet.getArgs();
				if (args.isEmpty()) {
					args = null;
				}
				Event event = new Event(packet.getName(), args);
				data = JsonObjectMapperProvider.getObjectMapper()
						.writeValueAsString(event);
				break;
	
			case JSON:
				data = JsonObjectMapperProvider.getObjectMapper()
						.writeValueAsString(packet.getData());
				break;
	
			case CONNECT:
				data = packet.getQs();
				break;
	
			case ACK:
				String dataStr = packet.getAckId();
				if (!packet.getArgs().isEmpty()) {
					dataStr += "+"
							+ JsonObjectMapperProvider.getObjectMapper()
									.writeValueAsString(packet.getArgs());
				}
				data = dataStr;
				break;
	
			case ERROR:
				int reasonCode = -1;
				int adviceCode = -1;
				if (packet.getReason() != null) {
					reasonCode = packet.getReason().getValue();
				}
				if (packet.getAdvice() != null) {
					adviceCode = packet.getAdvice().getValue();
				}
	
				if (reasonCode != -1 || adviceCode != -1) {
					StringBuilder errorData = new StringBuilder();
					if (reasonCode != -1) {
						errorData.append(reasonCode);
					}
					if (adviceCode != -1) {
						errorData.append("+").append(adviceCode);
					}
					data = errorData;
				}
				break;
			case NOOP:
			case DISCONNECT:
			case HEARTBEAT:
			default:
				/* Do nothing */
				break;
		}

		List<Object> params = new ArrayList<Object>(4);
		params.add(type);
		if ("data".equals(ack)) {
			params.add(id + "+");
		} else {
			params.add(id);
		}
		params.add(endpoint);
		if (data != null) {
			params.add(data);
		}

		return join(":", params);
	}

	private static String join(final String delimiter, final List<Object> args) {
		StringBuilder result = new StringBuilder();
		for (Iterator<Object> iterator = args.iterator(); iterator.hasNext();) {
			Object arg = iterator.next();
			result.append(arg);
			if (iterator.hasNext()) {
				result.append(delimiter);
			}
		}
		return result.toString();
	}

}
