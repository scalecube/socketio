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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.ConnectPacket;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.serialization.PacketDecoder;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;

/**
 * 
 * @author Anton Kharenko
 *
 */
@ChannelHandler.Sharable
public class WebSocketHandler extends ChannelInboundHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Map<Object, String> sessionIdByChannel = new ConcurrentHashMap<Object, String>();
	private final String connectPath;
	private final boolean secure;

	public WebSocketHandler(String handshakePath, boolean secure) {
		this.connectPath = handshakePath + getTransportType().getName();
		this.secure = secure;
	}

	protected TransportType getTransportType() {
		return TransportType.WEBSOCKET;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			FullHttpRequest req = (FullHttpRequest) msg;
			if (req.getMethod() == HttpMethod.GET && req.getUri().startsWith(connectPath)) {
				final QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
				final String requestPath = queryDecoder.path();

				log.debug("Received HTTP {} handshake request: {} {} from channel: {}", getTransportType().getName(), req.getMethod(),
						requestPath, ctx.channel());

				handshake(ctx, req, requestPath);

				ReferenceCountUtil.release(msg);
				return;
			}
		} else if (msg instanceof WebSocketFrame && isCurrentHandlerSession(ctx)) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
			ReferenceCountUtil.release(msg);
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

	private boolean handshake(final ChannelHandlerContext ctx, final FullHttpRequest req, final String requestPath) {
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
		WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
		if (handshaker != null) {
			handshaker.handshake(ctx.channel(), req).addListener(
                    new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        final String sessionId = PipelineUtils.getSessionId(requestPath);
                        connect(ctx, req, sessionId);
                    }
                }
            });
			return true;
		} else {
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		}
		return false;
	}

	private String getWebSocketLocation(HttpRequest req) {
		String protocol = secure ? "wss://" : "ws://";
		String webSocketLocation = protocol + req.headers().get(HttpHeaders.Names.HOST) + req.getUri();
		log.info("Created {} at: {}", getTransportType().getName(), webSocketLocation);
		return webSocketLocation;
	}

	private void connect(ChannelHandlerContext ctx, HttpRequest req, String sessionId) {
		sessionIdByChannel.put(ctx.channel(), sessionId);
		final ConnectPacket packet = new ConnectPacket(sessionId, PipelineUtils.getOrigin(req));
		packet.setTransportType(getTransportType());
		ctx.fireChannelRead(packet);
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
		log.debug("Received {} WebSocketFrame: {} from channel: {}", getTransportType().getName(), msg, ctx.channel());

		if (msg instanceof CloseWebSocketFrame) {
			sessionIdByChannel.remove(ctx.channel());
			ChannelFuture f = ctx.writeAndFlush(msg);
			f.addListener(ChannelFutureListener.CLOSE);
			return;
		} else if (msg instanceof PingWebSocketFrame) {
			ctx.writeAndFlush(new PongWebSocketFrame(msg.content()));
			return;
		} else if (!(msg instanceof TextWebSocketFrame)) {
			throw new UnsupportedOperationException(String.format("%s frame types not supported", msg.getClass().getName()));
		}

		TextWebSocketFrame frame = (TextWebSocketFrame) msg;
		Packet packet = PacketDecoder.decodePacket(frame.text());
		packet.setTransportType(getTransportType());
		String sessionId = sessionIdByChannel.get(ctx.channel());
		packet.setSessionId(sessionId);
		ctx.fireChannelRead(packet);
	}

}
