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

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;
import org.socketio.netty.session.IInternalSession;

public class HeartbeatHandler extends SimpleChannelUpstreamHandler {
	
	private final SocketIOSessionFactory sessionFactory;
	
	public HeartbeatHandler(SocketIOSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent event) throws Exception {
		Object message = event.getMessage();
		if (message instanceof Packet) {
			final Packet packet = (Packet) message;
			if (packet.getType() == PacketType.HEARTBEAT) {
				final String sessionId = packet.getSessionId(); 
				final IInternalSession session = sessionFactory.getSessionIfExist(sessionId);
				if (session != null) {
					session.acceptPacket(ctx.getChannel(), packet);
					session.acceptHeartbeat();
				}
				return;
			}
		}
		super.messageReceived(ctx, event);
	}
	
}
