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
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.ISession;
import org.socketio.netty.ISocketIOListener;
import org.socketio.netty.packets.ConnectPacket;
import org.socketio.netty.packets.IPacket;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;
import org.socketio.netty.session.IManagedSession;
import org.socketio.netty.session.ISessionDisconnectHandler;
import org.socketio.netty.storage.SessionStorage;

@Sharable
public class PacketDispatcherHandler extends SimpleChannelUpstreamHandler implements ISessionDisconnectHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final SessionStorage sessionFactory;
	
	private final ISocketIOListener listener;
	
	public PacketDispatcherHandler(SessionStorage sessionFactory, ISocketIOListener listener) {
		this.sessionFactory = sessionFactory;
		this.listener = listener;
	}

	@Override
	public void channelConnected(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
		log.debug("Channel connected: {}", ctx.getChannel());
		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(final ChannelHandlerContext ctx,final ChannelStateEvent e) throws Exception {
		log.debug("Channel disconnected: {}", ctx.getChannel());
		super.channelDisconnected(ctx, e);
	}

	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
		log.error("Exception caught at channel: {}", e.getChannel(), e.getCause());
	}
	
	@Override
	public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent event) throws Exception {
		Object message = event.getMessage();
		if (message instanceof IPacket) {
			final IPacket packet = (IPacket) message;
			try {
				final Channel channel = ctx.getChannel();
				log.debug("Dispatching packet: {} from channel: {}", packet, channel);
				this.dispatchPacket(channel, packet);
			} catch (Exception e) {
				log.error("Failed to dispatch packet: {}", packet, e);
			}
		} 
	}

	public void dispatchPacket(final Channel channel, final IPacket packet) throws Exception {
		if (packet instanceof ConnectPacket) {
			ConnectPacket connectPacket = (ConnectPacket) packet;
			final IManagedSession session = sessionFactory.getSession(connectPacket, channel, this);
			onConnectPacket(channel, session);
		} else if (packet instanceof Packet){
			Packet message = (Packet) packet;
			final String sessionId = packet.getSessionId(); 
			final IManagedSession session = sessionFactory.getSessionIfExist(sessionId);
			if (session != null) {
				onPacket(channel, session, message);
			}
		} else {
			throw new UnsupportedPacketTypeException(packet);
		}
	}

	private void onConnectPacket(final Channel channel, final IManagedSession session) {
		boolean initialConnect = session.connect(channel);
		if (initialConnect && listener != null) {
			listener.onConnect(session);
		}
	}

	private void onPacket(final Channel channel, final IManagedSession session, final Packet packet) {
		if (packet.getType() == PacketType.DISCONNECT) {
			session.disconnect(channel);
		} else {
			session.acceptPacket(channel, packet);
			if (listener != null) {
				if (packet.getType() == PacketType.MESSAGE) {
					listener.onMessage(session, packet.getData().toString());
				} else if (packet.getType() == PacketType.JSON) {
					listener.onJsonObject(session, packet.getData());
				}
			}
		}
	}
	
	@Override
	public void onSessionDisconnect(ISession session) {
		if (sessionFactory.containSession(session.getSessionId())) {
			log.debug("Client with sessionId: {} disconnected", session.getSessionId());
			sessionFactory.removeSession(session.getSessionId());
			if (listener != null) {
				listener.onDisconnect(session);
			}
		}
	}

}
