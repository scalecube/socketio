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

import java.io.IOException;
import java.util.UUID;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.ReferenceCountUtil;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * This class implements Socket.IO handshake procedure described below:
 *
 * <h1>Handshake</h1>
 *
 * <p>
 * The client will perform an initial HTTP POST request like the following
 * </p>
 *
 * <p>
 * {@code http://example.com/socket.io/1/}
 * </p>
 *
 * <p>
 * The absence of the transport id and session id segments will signal the
 * server this is a new, non-handshaken connection.
 * </p>
 *
 * <p>
 * The server can respond in three different ways:
 * </p>
 *
 * <p>
 * {@code 401 Unauthorized}
 * </p>
 *
 * <p>
 * If the server refuses to authorize the client to connect, based on the
 * supplied information (eg: Cookie header or custom query components).
 * </p>
 *
 * <p>
 * No response body is required.
 * </p>
 *
 * <p>
 * {@code 503 Service Unavailable}
 * </p>
 *
 * <p>
 * If the server refuses the connection for any reason (eg: overload).
 * </p>
 *
 * <p>
 * No response body is required.
 * </p>
 *
 * <p>
 * {@code 200 OK}
 * </p>
 *
 * <p>
 * The handshake was successful.
 * </p>
 *
 * <p>
 * The body of the response should contain the session id (sid) given to the
 * client, followed by the heartbeat timeout, the connection closing timeout,
 * and the list of supported transports separated by {@code :}
 * </p>
 *
 * <p>
 * The absence of a heartbeat timeout ('') is interpreted as the server and
 * client not expecting heartbeats.
 * </p>
 *
 * <p>
 * For example {@code 4d4f185e96a7b:15:10:websocket,xhr-polling}
 * </p>
 *
 * @author Anton Kharenko, Ronen Hamias
 *
 */
@ChannelHandler.Sharable
public class HandshakeHandler extends ChannelInboundHandlerAdapter {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String handshakePath;
  private final String commonHandshakeParameters;

  public HandshakeHandler(final String handshakePath, final int heartbeatTimeout, final int closeTimeout, final String transports) {
    this.handshakePath = handshakePath;
    commonHandshakeParameters = ":" + heartbeatTimeout + ":" + closeTimeout + ":" + transports;
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof HttpRequest) {
      final HttpRequest req = (HttpRequest) msg;
      final HttpMethod requestMethod = req.getMethod();
      final QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
      final String requestPath = queryDecoder.path();

      if (!requestPath.startsWith(handshakePath)) {
        log.warn("Received HTTP bad request: {} {} from channel: {}", requestMethod, requestPath, ctx.channel());

        HttpResponse res = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        f.addListener(ChannelFutureListener.CLOSE);
        ReferenceCountUtil.release(req);
        return;
      }

      if (HttpMethod.GET.equals(requestMethod) && requestPath.equals(handshakePath)) {
        if (log.isDebugEnabled())
          log.debug("Received HTTP handshake request: {} {} from channel: {}", requestMethod, requestPath, ctx.channel());

        handshake(ctx, req, queryDecoder);
        ReferenceCountUtil.release(req);
        return;
      }
    }

    super.channelRead(ctx, msg);
  }

  private void handshake(final ChannelHandlerContext ctx, final HttpRequest req, final QueryStringDecoder queryDecoder)
      throws IOException {
    // Generate session ID
    final String sessionId = UUID.randomUUID().toString();
    if (log.isDebugEnabled())
      log.debug("New sessionId: {} generated", sessionId);

    // Send handshake response
    final String handshakeMessage = getHandshakeMessage(sessionId, queryDecoder);

    ByteBuf content = PipelineUtils.copiedBuffer(ctx.alloc(), handshakeMessage);
    HttpResponse res = PipelineUtils.createHttpResponse(PipelineUtils.getOrigin(req), content, false);
    ChannelFuture f = ctx.writeAndFlush(res);
    f.addListener(ChannelFutureListener.CLOSE);
    if (log.isDebugEnabled())
      log.debug("Sent handshake response: {} to channel: {}", handshakeMessage, ctx.channel());
  }

  private String getHandshakeMessage(final String sessionId, final QueryStringDecoder queryDecoder) throws IOException {
    String jsonpParam = PipelineUtils.extractParameter(queryDecoder, "jsonp");
    String handshakeParameters = sessionId + commonHandshakeParameters;
    if (jsonpParam != null) {
      return "io.j[" + jsonpParam + "](\"" + handshakeParameters + "\");";
    } else {
      return handshakeParameters;
    }
  }

}
