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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.ReferenceCountUtil;
import io.scalecube.socketio.TransportType;
import io.scalecube.socketio.packets.ConnectPacket;
import io.scalecube.socketio.packets.Packet;
import io.scalecube.socketio.serialization.PacketDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Anton Kharenko
 *
 */
@ChannelHandler.Sharable
public class WebSocketHandler extends ChannelInboundHandlerAdapter {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final Map<Object, String> sessionIdByChannel = new ConcurrentHashMap<>();
  private final String connectPath;
  private final boolean secure;
  private final String remoteAddressHeader;
  private final int maxWebSocketFrameSize;

  public WebSocketHandler(final String handshakePath, final boolean secure, final int maxWebSocketFrameSize,
                          final String remoteAddressHeader) {
    this.connectPath = handshakePath + getTransportType().getName();
    this.secure = secure;
    this.remoteAddressHeader = remoteAddressHeader;
    this.maxWebSocketFrameSize = maxWebSocketFrameSize;
  }

  protected TransportType getTransportType() {
    return TransportType.WEBSOCKET;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof FullHttpRequest) {
      FullHttpRequest req = (FullHttpRequest) msg;
      if (req.method() == HttpMethod.GET && req.uri().startsWith(connectPath)) {
        final QueryStringDecoder queryDecoder = new QueryStringDecoder(req.uri());
        final String requestPath = queryDecoder.path();

        if (log.isDebugEnabled())
          log.debug("Received HTTP {} handshake request: {} from channel: {}", getTransportType().getName(), req, ctx.channel());

        try {
          handshake(ctx, req, requestPath);
        } catch (Exception e) {
          log.error("Error during {} handshake : {}", getTransportType().getName(), e);
        } finally {
          ReferenceCountUtil.release(msg);
        }
        return;
      }
    } else if (msg instanceof WebSocketFrame && isCurrentHandlerSession(ctx)) {
      handleWebSocketFrame(ctx, (WebSocketFrame) msg);
      return;
    }
    ctx.fireChannelRead(msg);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    sessionIdByChannel.remove(ctx.channel());
    super.channelInactive(ctx);
  }

  private boolean isCurrentHandlerSession(ChannelHandlerContext ctx) {
    return sessionIdByChannel.containsKey(ctx.channel());
  }

  private void handshake(final ChannelHandlerContext ctx, final FullHttpRequest req, final String requestPath) {
    WebSocketServerHandshakerFactory wsFactory =
        new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true, maxWebSocketFrameSize);
    WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
    if (handshaker != null) {
      handshaker.handshake(ctx.channel(), req).addListener(
          new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
              final String sessionId = PipelineUtils.getSessionId(requestPath);
              if (future.isSuccess()) {
                ctx.channel().pipeline().addBefore(
                    SocketIOChannelInitializer.SOCKETIO_WEBSOCKET_HANDLER,
                    SocketIOChannelInitializer.WEBSOCKET_FRAME_AGGREGATOR,
                    new WebSocketFrameAggregator(maxWebSocketFrameSize));
                connect(ctx, req, sessionId);
              } else {
                log.error("Can't handshake: {}", sessionId, future.cause());
              }
            }
          });
    } else {
      WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
    }
  }

  private String getWebSocketLocation(HttpRequest req) {
    String protocol = secure ? "wss://" : "ws://";
    String webSocketLocation = protocol + req.headers().get(HttpHeaderNames.HOST) + req.uri();
    if (log.isDebugEnabled())
      log.debug("Created {} at: {}", getTransportType().getName(), webSocketLocation);
    return webSocketLocation;
  }

  private void connect(ChannelHandlerContext ctx, HttpRequest req, String sessionId) throws Exception {
    sessionIdByChannel.put(ctx.channel(), sessionId);

    SocketAddress clientIp = PipelineUtils.resolveClientIpByRemoteAddressHeader(req, remoteAddressHeader);

    final ConnectPacket packet = new ConnectPacket(sessionId, PipelineUtils.getOrigin(req));
    packet.setTransportType(getTransportType());
    packet.setRemoteAddress(clientIp);

    ctx.fireChannelRead(packet);
  }

  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
    if (log.isDebugEnabled())
      log.debug("Received {} WebSocketFrame: {} from channel: {}", getTransportType().getName(), msg, ctx.channel());

    if (msg instanceof CloseWebSocketFrame) {
      sessionIdByChannel.remove(ctx.channel());
      ChannelFuture f = ctx.writeAndFlush(msg);
      f.addListener(ChannelFutureListener.CLOSE);
    } else if (msg instanceof PingWebSocketFrame) {
      ctx.writeAndFlush(new PongWebSocketFrame(msg.content()));
    } else if (msg instanceof TextWebSocketFrame || msg instanceof BinaryWebSocketFrame){
      Packet packet = PacketDecoder.decodePacket(msg.content());
      packet.setTransportType(getTransportType());
      String sessionId = sessionIdByChannel.get(ctx.channel());
      packet.setSessionId(sessionId);
      msg.release();
      ctx.fireChannelRead(packet);
    } else {
      msg.release();
      log.warn("{} frame type is not supported", msg.getClass().getName());
    }
  }

}
