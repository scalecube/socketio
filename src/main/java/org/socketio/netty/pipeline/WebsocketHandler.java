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

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.ConnectPacket;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.serialization.PacketDecoder;

/**
 * 
 * @author Anton Kharenko
 *
 */
public class WebsocketHandler extends SimpleChannelUpstreamHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final Map<Integer, String> sessionIdByChannel = new ConcurrentHashMap<Integer, String>();
	private final String connectWebsocketPath;
	private final boolean secure;
	
	public WebsocketHandler(String connectPath, boolean secure){
		this.connectWebsocketPath = connectPath + "websocket";
		this.secure = secure;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) msg;
    		if(req.getMethod() == HttpMethod.GET && req.getUri().startsWith(connectWebsocketPath)){
    			final QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
    			final String requestPath = queryDecoder.getPath();
    			final String sessionId = PipelineUtils.getSessionId(requestPath);
    			
    			boolean handshakeSuccess = handshake(ctx, req, sessionId);
    			if (handshakeSuccess) {
    				connect(ctx, req, sessionId);
    			}
    			return;
    		}
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            return;
        }
        ctx.sendUpstream(e);
	}
	
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		sessionIdByChannel.remove(ctx.getChannel().getId());
		super.channelDisconnected(ctx, e);
	}

	private boolean handshake(ChannelHandlerContext ctx, HttpRequest req, String sessionId) {
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req, sessionId), null, false);
		WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
		if (handshaker != null) {
			handshaker.handshake(ctx.getChannel(), req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
			return true;
		} else {
			wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
		}
		return false;
	}
	
	private String getWebSocketLocation(HttpRequest req, String sessionId) {
		String protocol = secure ? "wss://" : "ws://";
		String webSocketLocation = protocol + req.getHeader(HttpHeaders.Names.HOST) + connectWebsocketPath + "/" + sessionId;
		log.info("Created web socket at: {}", webSocketLocation);
		return webSocketLocation;
	}
	
	private void connect(ChannelHandlerContext ctx, HttpRequest req, String sessionId) {
		sessionIdByChannel.put(ctx.getChannel().getId(), sessionId);
		final ConnectPacket packet = new ConnectPacket(sessionId, PipelineUtils.getOrigin(req));
		packet.setTransportType(TransportType.WEBSOCKET);
		Channels.fireMessageReceived(ctx, packet);
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
		// Check for closing frame
        if (msg instanceof CloseWebSocketFrame) {
        	sessionIdByChannel.remove(ctx.getChannel().getId());
            ChannelFuture f = ctx.getChannel().write(msg);
            f.addListener(ChannelFutureListener.CLOSE);
            return;
        } else if (msg instanceof PingWebSocketFrame) {
            ctx.getChannel().write(new PongWebSocketFrame(msg.getBinaryData()));
            return;
        } else if (!(msg instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", msg.getClass()
                    .getName()));
        }
        
        TextWebSocketFrame frame = (TextWebSocketFrame) msg;
		Packet packet = PacketDecoder.decodePacket(frame.getText());
		packet.setTransportType(TransportType.WEBSOCKET);
		String sessionId = sessionIdByChannel.get(ctx.getChannel().getId());
		packet.setSessionId(sessionId);
		Channels.fireMessageReceived(ctx.getChannel(), packet);
	}
	
}
