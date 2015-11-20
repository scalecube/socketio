/**
 * Copyright 2012 Ronen Hamias, Anton Kharenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.scalecube.socketio.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.netty.channel.Channel;
import io.scalecube.socketio.TransportType;
import io.scalecube.socketio.packets.ConnectPacket;
import io.scalecube.socketio.pipeline.UnsupportedTransportTypeException;

/**
 *
 * @author Anton Kharenko
 *
 */
public class SessionStorage {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final ConcurrentMap<String, ManagedSession> sessions = new ConcurrentHashMap<>();

  private final int localPort;

  public SessionStorage(int localPort) {
    this.localPort = localPort;
  }

  public boolean containSession(final String sessionId) {
    return sessions.containsKey(sessionId);
  }

  public void removeSession(final String sessionId) {
    sessions.remove(sessionId);
  }

  public ManagedSession getSession(final ConnectPacket connectPacket,
                                    final Channel channel,
                                    final SessionDisconnectHandler disconnectHandler) throws Exception {

    ManagedSession session = getOrCreateSession(connectPacket, channel, disconnectHandler, null);

    // If transport protocol was changed then remove old session and create new one instead
    if (connectPacket.getTransportType() != session.getTransportType()) {
      session.markAsUpgraded();

      String oldSessionId = session.getSessionId();
      TransportType oldTransportType = session.getTransportType();

      final String sessionId = connectPacket.getSessionId();
      removeSession(sessionId);
      session = getOrCreateSession(connectPacket, channel, disconnectHandler, session.getTransportType());

      if (log.isDebugEnabled())
        log.debug("{} transport type {} session was upgraded to new transport type {} and session {}",
            oldTransportType.name(), oldSessionId, session.getTransportType().name(), session.getSessionId());
    }

    return session;
  }

  private ManagedSession getOrCreateSession(final ConnectPacket connectPacket,
                                            final Channel channel,
                                            final SessionDisconnectHandler disconnectHandler,
                                            final TransportType upgradedFromTransportType) throws Exception {
    final String sessionId = connectPacket.getSessionId();
    ManagedSession session = sessions.get(sessionId);
    if (session == null) {
      session = createSession(connectPacket, channel, disconnectHandler, upgradedFromTransportType);
      ManagedSession fasterSession = sessions.putIfAbsent(sessionId, session);
      if (fasterSession != null) {
        session = fasterSession;
      }
    }
    return session;
  }

  private ManagedSession createSession(final ConnectPacket connectPacket,
                                        final Channel channel,
                                        final SessionDisconnectHandler disconnectHandler,
                                        final TransportType upgradedFromTransportType) throws Exception {
    final TransportType transportType = connectPacket.getTransportType();
    final String sessionId = connectPacket.getSessionId();
    final String origin = connectPacket.getOrigin();
    final String jsonpIndexParam = connectPacket.getJsonpIndexParam();
    final SocketAddress remoteAddress = connectPacket.getRemoteAddress();

    // Create session by transport type
    switch(transportType) {
      case WEBSOCKET:
        return new WebSocketSession(channel, sessionId,
            origin, disconnectHandler, upgradedFromTransportType, localPort, remoteAddress);
      case FLASHSOCKET:
        return new FlashSocketSession(channel, sessionId,
            origin, disconnectHandler, upgradedFromTransportType, localPort, remoteAddress);
      case XHR_POLLING:
        return new XHRPollingSession(channel, sessionId,
            origin, disconnectHandler, upgradedFromTransportType, localPort, remoteAddress);
      case JSONP_POLLING:
        return new JsonpPollingSession(channel, sessionId,
            origin, disconnectHandler, upgradedFromTransportType, localPort, jsonpIndexParam, remoteAddress);
      default:
        throw new UnsupportedTransportTypeException(transportType);
    }
  }

  public ManagedSession getSessionIfExist(final String sessionId) {
    return sessions.get(sessionId);
  }
}
