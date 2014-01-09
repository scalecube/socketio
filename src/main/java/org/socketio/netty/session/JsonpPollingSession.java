package org.socketio.netty.session;

import org.jboss.netty.channel.Channel;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.IPacket;

public class JsonpPollingSession extends XHRPollingSession {
	
	private final String jsonpIndexParam;
	
	public JsonpPollingSession(
			Channel channel, 
			String sessionId,
			String origin, 
			ISessionDisconnectHandler disconnectHandler,
			TransportType upgradedFromTransportType, 
			int localPort,
			String jsonpIndexParam) {
		super(channel, sessionId, origin, disconnectHandler, upgradedFromTransportType,	localPort);
		this.jsonpIndexParam =jsonpIndexParam;  
	}

	@Override
	public TransportType getTransportType() {
		return TransportType.JSONP_POLLING;
	}

	@Override
	protected void fillPacketHeaders(IPacket packet) {
		super.fillPacketHeaders(packet);
		packet.setJsonpIndexParam(jsonpIndexParam);
	}

}
