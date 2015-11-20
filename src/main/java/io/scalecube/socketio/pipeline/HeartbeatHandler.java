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
package io.scalecube.socketio.pipeline;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.scalecube.socketio.packets.Packet;
import io.scalecube.socketio.packets.PacketType;
import io.scalecube.socketio.session.ManagedSession;
import io.scalecube.socketio.session.SessionStorage;

@ChannelHandler.Sharable
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {

  private final SessionStorage sessionFactory;

  public HeartbeatHandler(SessionStorage sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof Packet) {
      final Packet packet = (Packet) msg;
      if (packet.getType() == PacketType.HEARTBEAT) {
        final String sessionId = packet.getSessionId();
        final ManagedSession session = sessionFactory.getSessionIfExist(sessionId);
        if (session != null) {
          session.acceptPacket(ctx.channel(), packet);
          session.acceptHeartbeat();
        }
        return;
      }
    }
    super.channelRead(ctx, msg);
  }
}
