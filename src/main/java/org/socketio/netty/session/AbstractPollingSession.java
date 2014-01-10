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
		if (messagesQueue.isEmpty()) {
			outChannelHolder.set(channel);
		} else {
			PacketsFrame packetsFrame = messagesQueue.takeAll();
			sendPacketToChannel(channel, packetsFrame);
		}
	}

	@Override
	public ISessionFuture sendPacket(final Packet packet) {
		if (packet != null) {
			Channel channel = outChannelHolder.getAndSet(null);
			if (channel != null && channel.isConnected()) {
				sendPacketToChannel(channel, packet); 
			} else {
				fillPacketHeaders(packet);
				messagesQueue.add(packet);
			}
		}
		
		//TODO return correct Session Future for polling transports
		return null;
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
