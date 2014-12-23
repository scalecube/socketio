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
package org.socketio.netty.packets;

/**
 * Socket.IO packet type.
 * 
 * @author Ronen Hamias, Anton Kharenko
 * 
 */
public enum PacketType {

	/**
	 * <h1>(0) Disconnect</h1>
	 * 
	 * <p>
	 * Signals disconnection. If no endpoint is specified, disconnects the
	 * entire socket.
	 * </p>
	 * 
	 * <p>
	 * Examples:
	 * </p>
	 * 
	 * <p>
	 * Disconnect a socket connected to the /test endpoint:
	 * </p>
	 * 
	 * <p>
	 * {@code 0::/test}
	 * </p>
	 * 
	 * <p>
	 * Disconnect the whole socket:
	 * </p>
	 * 
	 * <p>
	 * {@code 0}
	 * </p>
	 */
	DISCONNECT(0),

	/**
	 * <h1>(1) Connect</h1>
	 * 
	 * <p>
	 * Only used for multiple sockets. Signals a connection to the endpoint.
	 * Once the server receives it, it's echoed back to the client.
	 * </p>
	 * 
	 * <p>
	 * Example, if the client is trying to connect to the endpoint /test, a
	 * message like this will be delivered:
	 * </p>
	 * 
	 * <p>
	 * {@code '1::' [path] [query]}
	 * </p>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <p>
	 * {@code 1::/test?my=param}
	 * </p>
	 * 
	 * <p>
	 * To acknowledge the connection, the server echoes back the message.
	 * Otherwise, the server might want to respond with a error packet.
	 * </p>
	 */
	CONNECT(1),

	/**
	 * <h1>(2) Heartbeat</h1>
	 * 
	 * <p>
	 * Sends a heartbeat. Heartbeats must be sent within the interval negotiated
	 * with the server. It's up to the client to decide the padding (for
	 * example, if the heartbeat timeout negotiated with the server is 20s, the
	 * client might want to send a heartbeat evert 15s).
	 * </p>
	 */
	HEARTBEAT(2),

	/**
	 * <h1>(3) Message</h1>
	 * 
	 * <p>
	 * {@code '3:' [message id ('+')] ':' [message endpoint] ':' [data]}
	 * </p>
	 * 
	 * <p>
	 * A regular message.
	 * </p>
	 * 
	 * <p>
	 * {@code 3:1::blabla}
	 * </p>
	 */
	MESSAGE(3),

	/**
	 * <h1>(4) JSON Message</h1>
	 * 
	 * <p>
	 * {@code '4:' [message id ('+')] ':' [message endpoint] ':' [json]}
	 * </p>
	 * 
	 * <p>
	 * A JSON encoded message.
	 * </p>
	 * 
	 * <p>
	 * {@code 4:1:: "a":"b"}
	 * </p>
	 */
	JSON(4),

	/**
	 * <h1>(5) Event</h1>
	 * 
	 * <p>
	 * {@code '5:' [message id ('+')] ':' [message endpoint] ':' [json encoded event]}
	 * </p>
	 * 
	 * <p>
	 * An event is like a json message, but has mandatory name and args fields.
	 * name is a string and args an array.
	 * </p>
	 * 
	 * <p>
	 * The event names:
	 * </p>
	 * 
	 * <ul>
	 * <li>{@code 'message'}</li>
	 * <li>{@code 'connect'}</li>
	 * <li>{@code 'disconnect'}</li>
	 * <li>{@code 'open'}</li>
	 * <li>{@code 'close'}</li>
	 * <li>{@code 'error'}</li>
	 * <li>{@code 'retry'}</li>
	 * <li>{@code 'reconnect'}</li>
	 * </ul>
	 * 
	 * <p>
	 * are reserved, and cannot be used by clients or servers with this message
	 * type.
	 * </p>
	 */
	EVENT(5),

	/**
	 * <h1>(6) ACK</h1>
	 * 
	 * <p>
	 * {@code '6:::' [message id] '+' [data]}
	 * </p>
	 * 
	 * <p>
	 * An acknowledgment contains the message id as the message data. If a +
	 * sign follows the message id, it's treated as an event message packet.
	 * </p>
	 * 
	 * <p>
	 * Example 1: simple acknowledgement
	 * </p>
	 * 
	 * <p>
	 * {@code 6:::4}
	 * </p>
	 * 
	 * <p>
	 * Example 2: complex acknowledgement
	 * </p>
	 * 
	 * <p>
	 * {@code 6:::4+["A","B"]}
	 * </p>
	 */
	ACK(6),

	/**
	 * <h1>(7) Error</h1>
	 * 
	 * <p>
	 * {@code '7::' [endpoint] ':' [reason] '+' [advice]}
	 * </p>
	 * 
	 * <p>
	 * For example, if a connection to a sub-socket is unauthorized.
	 * </p>
	 */
	ERROR(7),

	/**
	 * <h1>(8) Noop</h1>
	 * 
	 * <p>
	 * No operation. Used for example to close a poll after the polling duration
	 * times out.
	 * </p>
	 */
	NOOP(8);

	private static final int TYPES_SIZE = 9;
	private static final PacketType valueToType[] = new PacketType[TYPES_SIZE];

	static {
		for (PacketType type : values()) {
			valueToType[type.getValue()] = type;
		}
	}

	private final int value;
	private final String valueAsString;

	PacketType(final int value) {
		this.value = value;
		this.valueAsString = String.valueOf(value);
	}

	public int getValue() {
		return value;
	}

	public String getValueAsString() {
		return valueAsString;
	}

	public static PacketType valueOf(final int value) {
		return valueToType[value];
	}

}
