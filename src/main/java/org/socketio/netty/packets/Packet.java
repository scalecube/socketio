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

import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;

public class Packet extends AbstractPacket {

	public static final Packet NULL_INSTANCE = new Packet(null);

	private List<?> args = Collections.emptyList();
	private String qs;
	private Object ack;
	private String ackId;
	private String name;
	private String id = "";
	private String endpoint = "";
	private Object data;
	private int sequenceNumber = 0;
	
	private SocketAddress remoteAddress;

	private ErrorReason reason;
	private ErrorAdvice advice;

	public Packet(final PacketType type) {
		super(type);
	}

	public Packet(final PacketType type, final String sessionId) {
		super(type, sessionId);
	}

	public void setData(final Object data) {
		this.data = data;
	}

	public final Object getData() {
		return data;
	}

	public void setEndpoint(final String endpoint) {
		this.endpoint = endpoint;
	}

	public final String getEndpoint() {
		return endpoint;
	}

	public final String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public void setAck(final Object ack) {
		this.ack = ack;
	}

	public final Object getAck() {
		return ack;
	}

	public final String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public final List<?> getArgs() {
		return args;
	}

	public void setArgs(final List<?> args) {
		this.args = args;
	}

	public final String getQs() {
		return qs;
	}

	public void setQs(final String qs) {
		this.qs = qs;
	}

	public final String getAckId() {
		return ackId;
	}

	public void setAckId(final String ackId) {
		this.ackId = ackId;
	}

	public final ErrorReason getReason() {
		return reason;
	}

	public void setReason(final ErrorReason reason) {
		this.reason = reason;
	}

	public final ErrorAdvice getAdvice() {
		return advice;
	}

	public void setAdvice(final ErrorAdvice advice) {
		this.advice = advice;
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
	
	/**
	 * @return the remoteAddress
	 */
	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	/**
	 * @param remoteAddress the remoteAddress to set
	 */
	public void setRemoteAddress(SocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Packet [args=");
		builder.append(args);
		builder.append(", qs=");
		builder.append(qs);
		builder.append(", ack=");
		builder.append(ack);
		builder.append(", ackId=");
		builder.append(ackId);
		builder.append(", name=");
		builder.append(name);
		builder.append(", id=");
		builder.append(id);
		builder.append(", endpoint=");
		builder.append(endpoint);
		builder.append(", data=");
		builder.append(data);
		builder.append(", sequenceNumber=");
		builder.append(sequenceNumber);
		builder.append(", remoteAddress=");
		builder.append(remoteAddress);
		builder.append(", reason=");
		builder.append(reason);
		builder.append(", advice=");
		builder.append(advice);
		builder.append(", AbstractPacket.toString()=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}
}
