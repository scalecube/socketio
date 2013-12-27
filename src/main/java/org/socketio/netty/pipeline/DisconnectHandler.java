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
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;

/**
 * Class which provides handler for supporting forced socket disconnection
 * according to specification below.
 * 
 * <h1>Forced socket disconnection</h1>
 * <p/>
 * A Socket.IO server must provide an endpoint to force the disconnection of the
 * socket.
 * <p/>
 * While closing the transport connection is enough to trigger a disconnection,
 * it sometimes is desirable to make sure no timeouts are activated and the
 * disconnection events fire immediately.
 * <p/>
 * {@code http://example.com/socket.io/1/xhr-polling/812738127387123?disconnect}
 * <p/>
 * The server must respond with 200 OK, or 500 if a problem is detected.
 */
@Sharable
public class DisconnectHandler extends SimpleChannelUpstreamHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final String DISCONNECT = "disconnect";

	@Override
	public void messageReceived(final ChannelHandlerContext ctx,
			final MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			final HttpRequest req = (HttpRequest) msg;
			final HttpMethod requestMethod = req.getMethod();
			final QueryStringDecoder queryDecoder = new QueryStringDecoder(req.getUri());
			final String requestPath = queryDecoder.getPath();

			boolean disconnect = queryDecoder.getParameters().containsKey(DISCONNECT);
			if (disconnect) {
				log.debug("Received HTTP disconnect request: {} {} from channel: {}",
						new Object[] {requestMethod, requestPath, ctx.getChannel()});
				
				final String sessionId = PipelineUtils.getSessionId(requestPath);
				final Packet disconnectPacket = new Packet(PacketType.DISCONNECT, sessionId);
				disconnectPacket.setOrigin(PipelineUtils.getOrigin(req));
				Channels.fireMessageReceived(ctx, disconnectPacket);
				return;
			}
		}
		ctx.sendUpstream(e);
	}

}
