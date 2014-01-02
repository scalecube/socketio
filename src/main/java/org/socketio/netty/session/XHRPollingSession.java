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
package org.socketio.netty.session;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.Channel;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;
import org.socketio.netty.packets.PacketsFrame;

public class XHRPollingSession extends AbstractSession {
	
	private final Packet ackPacket = new Packet(PacketType.ACK);
	private final XHRPollingQueue messagesQueue = new XHRPollingQueue();
	private final AtomicReference<Channel> channelHolder = new AtomicReference<Channel>();

	public XHRPollingSession(final Channel channel, final String sessionId, final String origin, 
			final ISessionDisconnectHandler disconnectHandler, final TransportType upgradedFromTransportType, int localPort) {
		super(channel, sessionId, origin, disconnectHandler, upgradedFromTransportType, localPort);
		fillPacketHeaders(ackPacket);
	}
	
	@Override
	public TransportType getTransportType() {
		return TransportType.XHR_POLLING;
	}
	
	@Override
	public boolean connect(Channel channel) {
		boolean initialConnect = super.connect(channel);
		if (!initialConnect) {
			bindChannel(channel);
		}
		return initialConnect;
	}

	private void bindChannel(final Channel channel) {
		if (messagesQueue.isEmpty() && getState() != State.DISCONNECTING) {
			channelHolder.set(channel);
		} else {
			send(channel, null);
		}
	}

	@Override
	public void send(final Packet packet) {
		if (packet != null) {
			fillPacketHeaders(packet);
			Channel channel = channelHolder.getAndSet(null);
			send(channel, packet);
		}
	}

	private void send(final Channel channel, final Packet packet) {
		if (channel != null && channel.isConnected()) {
			if (getState() == State.DISCONNECTING) {
				disconnect(channel);
			} else if (packet != null) {
				channel.write(packet); 
			} else {
				PacketsFrame packetsFrame = messagesQueue.takeAll();
				fillPacketHeaders(packetsFrame);
				channel.write(packetsFrame);
			}
		} else if (packet != null) {
			messagesQueue.add(packet);
		}
	}
	
	@Override
	public void acceptPacket(final Channel channel, final Packet packet) {
		if (packet.getSequenceNumber() == 0 && channel.isConnected()) {
			channel.write(ackPacket);
		}
	}

	@Override
	public void disconnect() {
		if (getState() == State.DISCONNECTED) {
			return;
		}
		if (getState() != State.DISCONNECTING) {
			setState(State.DISCONNECTING);
			Channel channel = channelHolder.getAndSet(null);
			send(channel, null);
			
			// schedule forced disconnect
			heartbeatScheduler.scheduleDisconnect();
		} else {
			//force disconnect
			disconnect(null);
		}
	}
	
}
