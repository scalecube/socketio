package org.socketio.netty.session;

import org.jboss.netty.channel.Channel;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.Packet;

public abstract class AbstractSocketSession extends AbstractSession {

	private final Channel channel;

	public AbstractSocketSession(
			TransportType transport,
			Channel channel, 
			String sessionId, 
			String origin, 
			ISessionDisconnectHandler disconnectHandler, 
			final TransportType upgradedFromTransportType, 
			int localPort) {
		super(transport, channel, sessionId, origin, disconnectHandler, upgradedFromTransportType, localPort);
		this.channel = channel;
	}
	
	@Override
	public void send(Packet packet) {
		if (packet != null && channel != null && channel.isConnected()) {
			preparePacket(packet);
			channel.write(packet);
		}
	}

}
