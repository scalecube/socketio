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

import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.ISessionFuture;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.IPacket;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;

public abstract class AbstractSession implements IManagedSession {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final String sessionId;
	private final String origin;
	private final SocketAddress remoteAddress;
	private final TransportType upgradedFromTransportType;
	private final int localPort;
	
	private final Packet connectPacket = new Packet(PacketType.CONNECT);
	private final Packet disconnectPacket = new Packet(PacketType.DISCONNECT);
	private final Packet heartbeatPacket = new Packet(PacketType.HEARTBEAT);
	
	protected final ISessionDisconnectHandler disconnectHandler;
	protected final SocketIOHeartbeatScheduler heartbeatScheduler;
	
	private final AtomicReference<State> stateHolder = new AtomicReference<State>(State.CREATED);
	private volatile boolean upgraded = false;
	
	public AbstractSession (
			final Channel channel, 
			final String sessionId, 
			final String origin, 
			final ISessionDisconnectHandler disconnectHandler, 
			final TransportType upgradedFromTransportType, 
			final int localPort) {
		this.sessionId = sessionId;
		this.remoteAddress = channel.getRemoteAddress();
		this.origin = origin;
		this.localPort = localPort;
		this.disconnectHandler = disconnectHandler;
		this.upgradedFromTransportType = upgradedFromTransportType;
		heartbeatScheduler = new SocketIOHeartbeatScheduler(this);
		setState(State.CONNECTING);
	}
	
	@Override
	public final String getSessionId() {
		return sessionId;
	}
	
	@Override
	public final boolean isUpgradedSession() {
		return upgradedFromTransportType != null;
	}
	
	@Override
	public TransportType getUpgradedFromTransportType() {
		return upgradedFromTransportType;
	}

	@Override
	public final SocketAddress getRemoteAddress() {
		return remoteAddress;
	}
	
	@Override
	public final String getOrigin() {
		return origin;
	}
	
	@Override
	public int getLocalPort() {
		return localPort;
	}
	
	protected boolean isUpgraded() {
		return upgraded;
	}
	
	@Override
	public State getState() {
		return stateHolder.get();
	}
	
	@Override
	public boolean connect(final Channel channel) {
		heartbeatScheduler.reschedule();
		State previousState = setState(State.CONNECTED);
		boolean initialConnect = previousState == State.CONNECTING;
		if (initialConnect) {
			sendPacketToChannel(channel, connectPacket);
		}
		return initialConnect;
	}
	
	@Override
	public void disconnect(final Channel channel) {
		if (getState() == State.DISCONNECTED) {
			return;
		}
		
		setState(State.DISCONNECTING); 
		heartbeatScheduler.disableHeartbeat();
		if (!isUpgraded()) {
			sendPacket(disconnectPacket);
			disconnectHandler.onSessionDisconnect(this);
		}
		setState(State.DISCONNECTED);
	}
	
	@Override
	public void sendHeartbeat() {
		sendPacket(heartbeatPacket);
	}
	
	@Override
	public ISessionFuture send(final String message) {
		Packet messagePacket = new Packet(PacketType.MESSAGE);
        messagePacket.setData(message);
        return sendPacket(messagePacket);
	}
	
	protected ISessionFuture sendPacketToChannel(final Channel channel, IPacket packet) {
		try {
			fillPacketHeaders(packet);
			ChannelFuture channelFuture = channel.write(packet);
			return new DefaultSessionFuture(channelFuture, this);
		} catch (Exception e) {
			return new CompleteSessionFuture(this, false, e);
		}
	}
	
	@Override
	public void acceptPacket(final Channel channel, final Packet packet) {
	}
	
	public void markAsUpdgraded() {
		upgraded = true;
	}
	
	@Override
	public void acceptHeartbeat() {
		heartbeatScheduler.reschedule();
	}
	
	protected void fillPacketHeaders(IPacket packet) {
		packet.setOrigin(getOrigin());
		packet.setSessionId(getSessionId());
		packet.setTransportType(getTransportType());
	}
	
	protected State setState(final State state) {
		State previousState = stateHolder.getAndSet(state);
		if (previousState != state) {
			log.debug("Session {} state changed from {} to {}", new Object[] {getSessionId(), previousState, state});
		}
		return previousState;
	}
	
}
