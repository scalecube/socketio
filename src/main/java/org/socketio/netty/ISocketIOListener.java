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

import io.netty.buffer.ByteBuf;

/**
 * High level interface which abstracts Socket.IO transport implementation details
 * and publishes events to server services.
 */
public interface ISocketIOListener {

	/**
	 * Notify about new Socket.IO session established event.
	 *
	 * @param session the connected session
	 */
    void onConnect(final ISession session);

	/**
	 * Notify about arrival of new message. It is a responsibility of interface implementation
	 * to release provided message's byte buffer. In case if byte buffer won't be released it
	 * will cause memory leak.
	 *
	 * @param session session to which messages arrived
	 * @param message message's payload
	 */
    void onMessage(final ISession session, final ByteBuf message);

	/**
	 * Notify about Socket.IO session disconnection event.
	 *
	 * @param session the disconnected session
	 */
    void onDisconnect(final ISession session);

}
