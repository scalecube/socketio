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
package org.socketio.netty;

import java.net.SocketAddress;

/**
 * When client handshake and connects to the socket.io server a
 * session is created this session handles the communication with a
 * specific client connection each client is represented with specific
 * session id that is correlated in the communication protocol.
 * 
 * @author Ronen Hamias 
 */
public interface ISession {

	public enum State {
		CREATED, CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED
	}

	/**
	 * @return the session token for this client
	 */
	String getSessionId();

	/**
	 * @return the origin of this session
	 */
	String getOrigin();

	/**
	 * @return if this flag is true it means that session was created in result
	 *         of switching transport protocol (e.g. from WebSocket to
	 *         XHR-Polling).
	 */
	boolean isUpgradedSession();

	/**
	 * @return if that session was created in result of switching transport
	 *         protocol (e.g. from WebSocket to XHR-Polling) it will return
	 *         previous transport type. If this session isn't upgraded session
	 *         it will return null.
	 */
	TransportType getUpgradedFromTransportType();

	/**
	 * @return transport type for this session (e.g. WebSocket or XHR-POlling).
	 */
	TransportType getTransportType();

	/**
	 * the remote client address
	 * 
	 * @return the remote client address
	 */
	SocketAddress getRemoteAddress();
	
	/**
	 * session connected indicator
	 * 
	 * @return true if channel is connected
	 */
	State getState();
	
	/**
	 * @return local port of client connection   
 	 */
	int getLocalPort();
	
	/**
	 * send message to client
	 * 
	 * @param message
	 *            message to be sent to client
	 */
	void send(final String message);

	/**
	 * disconnect this session
	 */
	void disconnect();

}
