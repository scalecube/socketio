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
import org.socketio.netty.ISessionFuture;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;
import org.socketio.netty.packets.PacketsFrame;

public abstract class AbstractPollingSession extends AbstractSession {
	
	private final Packet ackPacket = new Packet(PacketType.ACK);
	private final PollingQueue messagesQueue = new PollingQueue();
	private final AtomicReference<Channel> outChannelHolder = new AtomicReference<Channel>();
	
	private DelayedSessionFuture delayedSendSessionFuture;
	
	public AbstractPollingSession(
			final Channel channel, 
			final String sessionId, 
			final String origin, 
			final ISessionDisconnectHandler disconnectHandler, 
			final TransportType upgradedFromTransportType, 
			int localPort) {
		super(channel, sessionId, origin, disconnectHandler, upgradedFromTransportType, localPort);
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
		if (getState() == State.DISCONNECTING) {
			disconnect(channel);
		} else {
			flush(channel);
		}
	}
	
	private void flush(final Channel channel) {
		ISessionFuture sessionFuture = null;
		DelayedSessionFuture currentDelayedSessionFuture = null;
		synchronized (messagesQueue) {
			if (messagesQueue.isEmpty()) {
				outChannelHolder.set(channel);
			} else {
				PacketsFrame packetsFrame = messagesQueue.takeAll();
				sessionFuture = sendPacketToChannel(channel, packetsFrame);
				currentDelayedSessionFuture = delayedSendSessionFuture;
				delayedSendSessionFuture = null;
			}
		}
		
		if (currentDelayedSessionFuture != null) {
			currentDelayedSessionFuture.initSessionFuture(sessionFuture);
		}
	}

	@Override
	public ISessionFuture sendPacket(final Packet packet) {
		if (packet == null) {
			return new CompleteSessionFuture(this, false, new NullPointerException("Packet is null"));
		}
		
		Channel channel = outChannelHolder.getAndSet(null);
		if (channel != null && channel.isConnected()) {
			return sendPacketToChannel(channel, packet); 
		} else {
			synchronized (messagesQueue) {
				messagesQueue.add(packet);
				
				if (delayedSendSessionFuture == null) {
					delayedSendSessionFuture = new DelayedSessionFuture(this);
				}
			}
			
			return delayedSendSessionFuture;
		}
	}
	
	@Override
	public void disconnect() {
		if (getState() == State.DISCONNECTED) {
			return;
		}
		if (getState() != State.DISCONNECTING) {
			setState(State.DISCONNECTING);
			
			// Check if there is active polling channel and disconnect 
			// otherwise schedule forced disconnect
			Channel channel = outChannelHolder.getAndSet(null);
			if (channel != null && channel.isConnected()) {
				disconnect(channel);
			} else {
				heartbeatScheduler.scheduleDisconnect();
			}
		} else {
			//forced disconnect
			disconnect(null);
		}
	}
	
	@Override
	public void acceptPacket(final Channel channel, final Packet packet) {
		if (packet.getSequenceNumber() == 0) {
			sendPacketToChannel(channel, ackPacket);
		}
	}
	
}
