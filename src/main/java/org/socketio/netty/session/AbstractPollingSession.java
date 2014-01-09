package org.socketio.netty.session;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.Channel;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;
import org.socketio.netty.packets.PacketsFrame;

public abstract class AbstractPollingSession extends AbstractSession {
	
	private final Packet ackPacket = new Packet(PacketType.ACK);
	private final PollingQueue messagesQueue = new PollingQueue();
	private final AtomicReference<Channel> channelHolder = new AtomicReference<Channel>();

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
	protected void initSession() {
		super.initSession();
		fillPacketHeaders(ackPacket);
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
