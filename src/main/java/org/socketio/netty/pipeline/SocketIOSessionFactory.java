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
package org.socketio.netty.pipeline;

import org.jboss.netty.channel.Channel;
import org.socketio.netty.TransportType;
import org.socketio.netty.memoizer.Computable;
import org.socketio.netty.memoizer.MemoizerConcurrentMap;
import org.socketio.netty.packets.ConnectPacket;
import org.socketio.netty.session.IInternalSession;
import org.socketio.netty.session.ISessionDisconnectHandler;
import org.socketio.netty.session.websocket.WebsocketSession;
import org.socketio.netty.session.xhr.XHRPollingSession;

/**
 * 
 * @author Anton Kharenko
 * 
 */
public class SocketIOSessionFactory {

	private final MemoizerConcurrentMap<String, IInternalSession> sessionsMemoizer = new MemoizerConcurrentMap<String, IInternalSession>();
	
	private final int localPort;
	
	public SocketIOSessionFactory(int localPort) {
		this.localPort = localPort;
	}

	public boolean containSession(final String sessionId) {
		return sessionsMemoizer.containsKey(sessionId);
	}
	
	public void removeSession(final String sessionId) {
		sessionsMemoizer.remove(sessionId);
	}

	public IInternalSession getSession(final ConnectPacket connectPacket,
			final Channel channel,
			final ISessionDisconnectHandler disconnectHandler) throws Exception {
		
		IInternalSession session = createSession(connectPacket, channel, disconnectHandler, null);
		
		// If transport protocol was changed then remove old session and create new one instead
		if (connectPacket.getTransportType() != session.getTransportType()) {
			session.discard();
			final String sessionId = connectPacket.getSessionId();
			removeSession(sessionId);
			session = createSession(connectPacket, channel, disconnectHandler, session.getTransportType());
		}
		
		return session;
	}
	
	private IInternalSession createSession(final ConnectPacket connectPacket,
			final Channel channel,
			final ISessionDisconnectHandler disconnectHandler,
			final TransportType upgradedFromTransportType) throws Exception {
		final TransportType transportType = connectPacket.getTransportType();
		final String sessionId = connectPacket.getSessionId();
		final String origin = connectPacket.getOrigin();
		try {
			return sessionsMemoizer.get(sessionId,
					new Computable<String, IInternalSession>() {
						@Override
						public IInternalSession compute(String sessionId) 	throws Exception {
							if (transportType == TransportType.WEBSOCKET) {
								return new WebsocketSession(channel, sessionId,
										origin, disconnectHandler, upgradedFromTransportType, localPort);
							} else {
								return new XHRPollingSession(channel,sessionId, 
										origin, disconnectHandler, upgradedFromTransportType, localPort);
							}
						}
					});
		} catch (Exception e) {
			throw new Exception(String.format(
					"Failed to create new session: %s",
					connectPacket.toString()), e);
		}
	}

	public IInternalSession getSessionIfExist(final String sessionId) {
		IInternalSession session = null;
		try {
			session = sessionsMemoizer.containsKey(sessionId) ? sessionsMemoizer
					.get(sessionId) : null;
		} catch (Exception e) {
		}
		return session;
	}
}
