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
package org.socketio.netty.session;

import org.jboss.netty.channel.Channel;
import org.socketio.netty.ISession;
import org.socketio.netty.packets.Packet;

/**
 * 
 * @author Anton Kharenko
 *
 */
public interface IInternalSession extends ISession {
	
	/**
	 * Connects current session to given channel.
	 * 
	 * @param channel
	 *            channel to which client connected
	 * @return true if channel was connected or false in case if channel was
	 *         already connected before.
	 */
	boolean connect(final Channel channel);
	
	/**
	 * disconnect this session
	 * @param channel the channel to use for disconnection
	 */
	void disconnect(final Channel channel);
	
	/**
	 * send heartbeat packet to client
	 */
	void sendHeartbeat();
	
	/**
	 * send packet message to client
	 * @param messagePacket message to be sent to client
	 */
	void send(final Packet messagePacket);
	
	/**
	 * send acknowlagment HTTP 200 to client that message was accepted
	 * @param channel
	 */
	void acceptPacket(final Channel channel, final Packet packet);
	
	void acceptHeartbeat();
	
	void discard();
	
	void setState(State state);
}
