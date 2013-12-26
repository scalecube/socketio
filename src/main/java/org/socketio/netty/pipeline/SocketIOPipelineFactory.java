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

import static org.jboss.netty.channel.Channels.pipeline;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.netty.handler.ssl.SslHandler;
import org.socketio.netty.ISocketIOListener;
import org.socketio.netty.TransportType;
import org.socketio.netty.storage.SessionStorage;

public class SocketIOPipelineFactory implements ChannelPipelineFactory {

	private static final int PROTOCOL = 1;
	private static final String CONTEXT_PATH = "/socket.io";
	private static final String CONNECT_PATH = CONTEXT_PATH + "/" + PROTOCOL + "/";

	private static final PacketEncoderHandler packetEncoderHandler = new PacketEncoderHandler();
	private static final XHRPollingConnectHandler xhrConnectionHanler = new XHRPollingConnectHandler(CONNECT_PATH);
	private static final XHRPollingPacketDecoderHandler xhrPacketDecoderHandler = new XHRPollingPacketDecoderHandler();
	private static final ExecutionHandler executionHandler = new ExecutionHandler(
			new OrderedMemoryAwareThreadPoolExecutor(16, 1048576, 1048576));
	private static final DisconnectHandler disconnectionHanler = new DisconnectHandler();
	private static final FlashPolicyHandler flashPolicyHandler = new FlashPolicyHandler();
	private static final ResourceHandler resourceHandler = new ResourceHandler(CONTEXT_PATH);

	private final SessionStorage sessionFactory;
	private final WebSocketHandler websocketHandler;
	private final HandshakeHandler handshakeHanler;
	private final HeartbeatHandler heartbeatHandler;
	private final PacketDispatcherHandler packetDispatcherHandler;
	private final SSLContext sslContext;
	private final boolean isFlashSupported;

	public SocketIOPipelineFactory(
			final ISocketIOListener listener,
			final int heartbeatTimeout, 
			final int closeTimeout,
			final String transports, 
			final SSLContext sslContext,
			final boolean alwaysSecureWebSocketLocation, 
			final int localPort) {
		this.sessionFactory = new SessionStorage(localPort);
		this.handshakeHanler = new HandshakeHandler(CONNECT_PATH, heartbeatTimeout, closeTimeout, transports);
		this.heartbeatHandler = new HeartbeatHandler(sessionFactory);
		this.packetDispatcherHandler = new PacketDispatcherHandler(sessionFactory, listener);
		this.sslContext = sslContext;

		final boolean secure = (sslContext != null) || alwaysSecureWebSocketLocation;
		websocketHandler = new WebSocketHandler(CONNECT_PATH, secure);
		
		isFlashSupported = transports.contains(TransportType.FLASHSOCKET.getName());
	}

	@Override
	public final ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();

		//TODO: Is flash policy handler should be before SSL handler or after?
		if (isFlashSupported) {
			pipeline.addLast("flash-policy", flashPolicyHandler);
		}
		
		/*
		 * SSL
		 */
		if (sslContext != null) {
			SSLEngine sslEngine = sslContext.createSSLEngine();
			sslEngine.setUseClientMode(false);
			SslHandler sslHandler = new SslHandler(sslEngine);
			sslHandler.setIssueHandshake(true);
			pipeline.addLast("ssl", sslHandler);
		}

		/*
		 * Downstream
		 */

		// HTTP downstream
		pipeline.addLast("http-response-encoder", new HttpResponseEncoder());

		// Socket.IO downstream
		pipeline.addLast("socketio-packet-encoder", packetEncoderHandler);

		/*
		 * Upstream
		 */

		// HTTP upstream
		pipeline.addLast("http-request-decoder", new HttpRequestDecoder());
		pipeline.addLast("http-chunk-aggregator", new HttpChunkAggregator(1048576));
		
		// Static content (.SWF files) for flash sockets  
		if (isFlashSupported) {
			pipeline.addLast("resource-handler", resourceHandler);
		}
		
		// Socket.IO upstream
		pipeline.addLast("socketio-handshake-handler", handshakeHanler);
		pipeline.addLast("socketio-disconnection-handler", disconnectionHanler);
		pipeline.addLast("socketio-websocket-handler", websocketHandler);
		pipeline.addLast("socketio-xhr-connect-handler", xhrConnectionHanler);
		pipeline.addLast("socketio-xhr-packet-decoder", xhrPacketDecoderHandler);
		pipeline.addLast("socketio-heartbeat-handler", heartbeatHandler);
		pipeline.addLast("socketio-execution-handler", executionHandler);
		pipeline.addLast("socketio-packet-dispatcher", packetDispatcherHandler);

		return pipeline;
	}

}
