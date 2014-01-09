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
 * According to socket.io spec Connect packet is sent to socket.io client after handshake.
 * 
 * @author Ronen Hamias
 *  
 */
public class ConnectPacket extends Packet {
	
	public ConnectPacket(final String sessionId) {
		super(PacketType.CONNECT, sessionId);
	}

	public ConnectPacket(final String sessionId,final String origin) {
		this(sessionId);
		this.setOrigin(origin);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConnectPacket [");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

}

