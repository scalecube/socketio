package org.socketio.netty.session;

import org.jboss.netty.channel.Channel;
import org.socketio.netty.TransportType;

public class JsonpPollingSession extends XHRPollingSession {

	public JsonpPollingSession(
			Channel channel, 
			String sessionId,
			String origin, 
			ISessionDisconnectHandler disconnectHandler,
			TransportType upgradedFromTransportType, 
			int localPort) {
		super(channel, sessionId, origin, disconnectHandler, upgradedFromTransportType,	localPort);
	}

	@Override
	public TransportType getTransportType() {
		return TransportType.JSONP_POLLING;
	}

}
