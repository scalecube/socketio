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

import java.util.concurrent.Executor;

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
	
	// Handler names
	public static final String FLASH_POLICY_HANDLER = "flash-policy-handler";
	public static final String SSL_HANDLER = "ssl-handler";
	public static final String HTTP_REPONSE_ENCODER = "http-response-encoder";
	public static final String HTTP_REQUEST_DECODER = "http-request-decoder";
	public static final String HTTP_CHUNK_AGGREGATOR = "http-chunk-aggregator";
	public static final String FLASH_RESOURCE_HANDLER = "flash-resource-handler";
	public static final String SOCKETIO_PACKET_ENCODER = "socketio-packet-encoder";
	public static final String SOCKETIO_HANDSHAKE_HANDLER = "socketio-handshake-handler";
	public static final String SOCKETIO_DISCONNECT_HANDLER = "socketio-disconnect-handler";
	public static final String SOCKETIO_WEBSOCKET_HANDLER = "socketio-websocket-handler";
	public static final String SOCKETIO_FLASHSOCKET_HANDLER = "socketio-flashsocket-handler";
	public static final String SOCKETIO_XHR_POLLING_HANDLER = "socketio-xhr-polling-handler";
	public static final String SOCKETIO_JSONP_POLLING_HANDLER = "socketio-jsonp-polling-handler";
	public static final String SOCKETIO_HEARTBEAT_HANDLER = "socketio-heartbeat-handler";
	public static final String EXECUTION_HANDLER = "execution-handler";
	public static final String SOCKETIO_PACKET_DISPATCHER = "socketio-packet-dispatcher";
	
	// Constant parameters
	private static final int PROTOCOL = 1;
	private static final String CONTEXT_PATH = "/socket.io";
	private static final String HANDSHAKE_PATH = CONTEXT_PATH + "/" + PROTOCOL + "/";
	private static final int MAX_HTTP_CONTENT_LENGTH = 1048576;
	private static final String FLASH_SOCKET_RESOURCE_PATH = "/static/flashsocket/WebSocketMain.swf";
	private static final String FLASH_SOCKET_INSECURE_RESOURCE_PATH = "/static/flashsocket/WebSocketMainInsecure.swf";
	private static final int EXECUTOR_CORE_POOL_SIZE = 16;
	private static final long EXECUTOR_MAX_CHANNEL_MEMORY_SIZE = 1048576L;
	private static final long EXECUTOR_MAX_TOTAL_MEMORY_SIZE = 1048576L;

	// Sharable handlers
	private final FlashPolicyHandler flashPolicyHandler;
	private final ResourceHandler flashResourceHandler;
	private final PacketEncoderHandler packetEncoderHandler;
	private final HandshakeHandler handshakeHanler;
	private final DisconnectHandler disconnectHanler;
	private final WebSocketHandler webSocketHandler;
	private final FlashSocketHandler flashSocketHandler;
	private final XHRPollingHandler xhrPollingHanler;
	private final JsonpPollingHandler jsonpPollingHanler;
	private final HeartbeatHandler heartbeatHandler;
	private final ExecutionHandler executionHandler;
	private final PacketDispatcherHandler packetDispatcherHandler;
	
	// State variables
	private final SessionStorage sessionFactory;
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
		// Initialize state variables
		this.sslContext = sslContext;
		sessionFactory = new SessionStorage(localPort);
		isFlashSupported = transports.contains(TransportType.FLASHSOCKET.getName());
		
		// Initialize sharable handlers
		flashPolicyHandler = new FlashPolicyHandler();
		
		flashResourceHandler = new ResourceHandler();
		flashResourceHandler.addResource(CONTEXT_PATH + FLASH_SOCKET_RESOURCE_PATH, FLASH_SOCKET_RESOURCE_PATH);
		flashResourceHandler.addResource(CONTEXT_PATH + FLASH_SOCKET_INSECURE_RESOURCE_PATH, FLASH_SOCKET_INSECURE_RESOURCE_PATH);
		
		packetEncoderHandler = new PacketEncoderHandler();
		
		handshakeHanler = new HandshakeHandler(HANDSHAKE_PATH, heartbeatTimeout, closeTimeout, transports);
		disconnectHanler = new DisconnectHandler();
		heartbeatHandler = new HeartbeatHandler(sessionFactory);
		
		final boolean secure = (sslContext != null) || alwaysSecureWebSocketLocation;
		webSocketHandler = new WebSocketHandler(HANDSHAKE_PATH, secure);
		flashSocketHandler = new FlashSocketHandler(HANDSHAKE_PATH, secure);
		
		xhrPollingHanler = new XHRPollingHandler(HANDSHAKE_PATH);
		jsonpPollingHanler = new JsonpPollingHandler(HANDSHAKE_PATH);
		
		Executor executor = new OrderedMemoryAwareThreadPoolExecutor(
				EXECUTOR_CORE_POOL_SIZE, EXECUTOR_MAX_CHANNEL_MEMORY_SIZE, EXECUTOR_MAX_TOTAL_MEMORY_SIZE); 
		executionHandler = new ExecutionHandler(executor);
		
		packetDispatcherHandler = new PacketDispatcherHandler(sessionFactory, listener);
	}

	@Override
	public final ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();

		// Flash policy file
		if (isFlashSupported) {
			pipeline.addLast(FLASH_POLICY_HANDLER, flashPolicyHandler);
		}
		
		// SSL
		if (sslContext != null) {
			SSLEngine sslEngine = sslContext.createSSLEngine();
			sslEngine.setUseClientMode(false);
			SslHandler sslHandler = new SslHandler(sslEngine);
			sslHandler.setIssueHandshake(true);
			pipeline.addLast(SSL_HANDLER, sslHandler);
		}

		// HTTP
		pipeline.addLast(HTTP_REPONSE_ENCODER, new HttpResponseEncoder());
		pipeline.addLast(HTTP_REQUEST_DECODER, new HttpRequestDecoder());
		pipeline.addLast(HTTP_CHUNK_AGGREGATOR, new HttpChunkAggregator(MAX_HTTP_CONTENT_LENGTH));
		
		// Flash resources  
		if (isFlashSupported) {
			pipeline.addLast(FLASH_RESOURCE_HANDLER, flashResourceHandler);
		}
		
		// Socket.IO
		pipeline.addLast(SOCKETIO_PACKET_ENCODER, packetEncoderHandler);
		pipeline.addLast(SOCKETIO_HANDSHAKE_HANDLER, handshakeHanler);
		pipeline.addLast(SOCKETIO_DISCONNECT_HANDLER, disconnectHanler);
		pipeline.addLast(SOCKETIO_WEBSOCKET_HANDLER, webSocketHandler);
		pipeline.addLast(SOCKETIO_FLASHSOCKET_HANDLER, flashSocketHandler);
		pipeline.addLast(SOCKETIO_XHR_POLLING_HANDLER, xhrPollingHanler);
		pipeline.addLast(SOCKETIO_JSONP_POLLING_HANDLER, jsonpPollingHanler);
		pipeline.addLast(SOCKETIO_HEARTBEAT_HANDLER, heartbeatHandler);
		pipeline.addLast(EXECUTION_HANDLER, executionHandler);
		pipeline.addLast(SOCKETIO_PACKET_DISPATCHER, packetDispatcherHandler);

		return pipeline;
	}

}
