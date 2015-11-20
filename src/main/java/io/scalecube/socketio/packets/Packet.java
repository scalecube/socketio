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
package io.scalecube.socketio.packets;

import io.netty.buffer.ByteBuf;

public class Packet extends AbstractPacket {

	public static final Packet NULL_INSTANCE = new Packet(null);

	private ByteBuf data;
	private int sequenceNumber = 0;

	public Packet(final PacketType type) {
		super(type);
	}

	public Packet(final PacketType type, final String sessionId) {
		super(type, sessionId);
	}

	public void setData(final ByteBuf data) {
		this.data = data;
	}

	public final ByteBuf getData() {
		return data;
	}

	/**
	 * The order number of packet in packets frame. In case if packet wasn't in
	 * packet frame then it will return 0.
	 */
	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public String toString() {
		return "Packet [" + super.toString() + ", data=" + data + "]";
	}

}
