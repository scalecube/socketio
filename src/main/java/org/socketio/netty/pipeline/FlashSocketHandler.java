package org.socketio.netty.pipeline;

import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.socketio.netty.TransportType;

/**
 * 
 * @author Anton Kharenko
 *
 */
@Sharable
public class FlashSocketHandler extends WebSocketHandler {

	public FlashSocketHandler(String handshakePath, boolean secure) {
		super(handshakePath, secure);
	}

	@Override
	protected TransportType getTransportType() {
		return TransportType.FLASHSOCKET;
	}

}
