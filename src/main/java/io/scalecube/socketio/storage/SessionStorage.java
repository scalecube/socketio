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
package io.scalecube.socketio.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

import io.netty.channel.Channel;
import io.scalecube.socketio.TransportType;
import io.scalecube.socketio.packets.ConnectPacket;
import io.scalecube.socketio.pipeline.UnsupportedTransportTypeException;
import io.scalecube.socketio.session.FlashSocketSession;
import io.scalecube.socketio.session.ManagedSession;
import io.scalecube.socketio.session.SessionDisconnectHandler;
import io.scalecube.socketio.session.JsonpPollingSession;
import io.scalecube.socketio.session.WebSocketSession;
import io.scalecube.socketio.session.XHRPollingSession;
import io.scalecube.socketio.storage.memoizer.Computable;
import io.scalecube.socketio.storage.memoizer.MemoizerConcurrentMap;

/**
 *
 * @author Anton Kharenko
 *
 */
public class SessionStorage {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final MemoizerConcurrentMap<String, ManagedSession> sessionsMemoizer = new MemoizerConcurrentMap<>();

  private final int localPort;

  public SessionStorage(int localPort) {
    this.localPort = localPort;
  }

  public boolean containSession(final String sessionId) {
    return sessionsMemoizer.containsKey(sessionId);
  }

  public void removeSession(final String sessionId) {
    sessionsMemoizer.remove(sessionId);
  }

  public ManagedSession getSession(final ConnectPacket connectPacket,
                                    final Channel channel,
                                    final SessionDisconnectHandler disconnectHandler) throws Exception {

    ManagedSession session = createSession(connectPacket, channel, disconnectHandler, null);

    // If transport protocol was changed then remove old session and create new one instead
    if (connectPacket.getTransportType() != session.getTransportType()) {
      session.markAsUpgraded();

      String oldSessionId = session.getSessionId();
      TransportType oldTransportType = session.getTransportType();

      final String sessionId = connectPacket.getSessionId();
      removeSession(sessionId);
      session = createSession(connectPacket, channel, disconnectHandler, session.getTransportType());

      if (log.isDebugEnabled())
        log.debug("{} transport type {} session was upgraded to new transport type {} and session {}",
            oldTransportType.name(), oldSessionId, session.getTransportType().name(), session.getSessionId());
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
    try {
      return sessionsMemoizer.get(sessionId,
          new Computable<String, ManagedSession>() {
            @Override
            public ManagedSession compute(String sessionId) throws Exception {
              if (transportType == TransportType.WEBSOCKET) {
                return new WebSocketSession(channel, sessionId,
                    origin, disconnectHandler, upgradedFromTransportType, localPort, remoteAddress);
              } else if (transportType == TransportType.FLASHSOCKET) {
                return new FlashSocketSession(channel, sessionId,
                    origin, disconnectHandler, upgradedFromTransportType, localPort, remoteAddress);
              } else if (transportType == TransportType.XHR_POLLING) {
                return new XHRPollingSession(channel, sessionId,
                    origin, disconnectHandler, upgradedFromTransportType, localPort, remoteAddress);
              } else if (transportType == TransportType.JSONP_POLLING) {
                return new JsonpPollingSession(channel, sessionId,
                    origin, disconnectHandler, upgradedFromTransportType, localPort, jsonpIndexParam, remoteAddress);
              } else {
                throw new UnsupportedTransportTypeException(transportType);
              }
            }
          });
    } catch (Exception e) {
      throw new Exception(String.format(
          "Failed to create new session: %s",
          connectPacket.toString()), e);
    }
  }

  public ManagedSession getSessionIfExist(final String sessionId) {
    ManagedSession session = null;
    try {
      session = sessionsMemoizer.containsKey(sessionId) ? sessionsMemoizer
          .get(sessionId) : null;
    } catch (Exception ignore) {
    }
    return session;
  }
}
