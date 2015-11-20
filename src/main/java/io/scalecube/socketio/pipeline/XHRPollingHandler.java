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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.List;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.ReferenceCountUtil;
import io.scalecube.socketio.TransportType;
import io.scalecube.socketio.packets.ConnectPacket;
import io.scalecube.socketio.packets.Packet;
import io.scalecube.socketio.serialization.PacketFramer;

@ChannelHandler.Sharable
public class XHRPollingHandler extends ChannelInboundHandlerAdapter {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String connectPath;
  private final String headerClientIpAddressName;

  public XHRPollingHandler(final String handshakePath, final String headerClientIpAddressName) {
    this.connectPath = handshakePath + TransportType.XHR_POLLING.getName();
    this.headerClientIpAddressName = headerClientIpAddressName;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      final FullHttpRequest req = (FullHttpRequest) msg;
      final HttpMethod requestMethod = req.getMethod();
      final QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
      final String requestPath = queryDecoder.path();

      if (requestPath.startsWith(connectPath)) {
        if (log.isDebugEnabled())
          log.debug("Received HTTP XHR-Polling request: {} {} from channel: {}", requestMethod, requestPath, ctx.channel());

        final String sessionId = PipelineUtils.getSessionId(requestPath);
        final String origin = PipelineUtils.getOrigin(req);

        if (HttpMethod.GET.equals(requestMethod)) {
          SocketAddress clientIp = PipelineUtils.getHeaderClientIPParamValue(req, headerClientIpAddressName);

          // Process polling request from client
          final ConnectPacket packet = new ConnectPacket(sessionId, origin);
          packet.setTransportType(TransportType.XHR_POLLING);
          packet.setRemoteAddress(clientIp);

          ctx.fireChannelRead(packet);
        } else if (HttpMethod.POST.equals(requestMethod)) {
          // Process message request from client
          List<Packet> packets = PacketFramer.decodePacketsFrame(req.content());
          for (Packet packet : packets) {
            packet.setSessionId(sessionId);
            packet.setOrigin(origin);
            ctx.fireChannelRead(packet);
          }
        } else {
          log.warn("Can't process HTTP XHR-Polling request. Unknown request method: {} from channel: {}", requestMethod,
              ctx.channel());
        }
        ReferenceCountUtil.release(msg);
        return;
      }
    }

    ctx.fireChannelRead(msg);
  }

}
