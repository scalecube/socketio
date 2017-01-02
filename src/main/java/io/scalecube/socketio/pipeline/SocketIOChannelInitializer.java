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

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.scalecube.socketio.PipelineModifier;
import io.scalecube.socketio.SocketIOListener;
import io.scalecube.socketio.ServerConfiguration;
import io.scalecube.socketio.TransportType;
import io.scalecube.socketio.session.SessionStorage;

public class SocketIOChannelInitializer extends ChannelInitializer {

  // Handler names
  public static final String FLASH_POLICY_HANDLER = "flash-policy-handler";
  public static final String SSL_HANDLER = "ssl-handler";
  public static final String HTTP_RESPONSE_ENCODER = "http-response-encoder";
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
  public static final String SOCKETIO_PACKET_DISPATCHER = "socketio-packet-dispatcher";

  // Constant parameters
  private static final int PROTOCOL = 1;
  private static final String CONTEXT_PATH = "/socket.io";
  private static final String HANDSHAKE_PATH = CONTEXT_PATH + "/" + PROTOCOL + "/";
  private static final int MAX_HTTP_CONTENT_LENGTH = 1048576;
  private static final String FLASH_SOCKET_RESOURCE_PATH = "/static/flashsocket/WebSocketMain.swf";
  private static final String FLASH_SOCKET_INSECURE_RESOURCE_PATH = "/static/flashsocket/WebSocketMainInsecure.swf";

  // Sharable handlers
  private final FlashPolicyHandler flashPolicyHandler;
  private final ResourceHandler flashResourceHandler;
  private final PacketEncoderHandler packetEncoderHandler;
  private final HandshakeHandler handshakeHandler;
  private final DisconnectHandler disconnectHandler;
  private final WebSocketHandler webSocketHandler;
  private final FlashSocketHandler flashSocketHandler;
  private final XHRPollingHandler xhrPollingHandler;
  private final JsonpPollingHandler jsonpPollingHandler;
  private final HeartbeatHandler heartbeatHandler;
  private final EventExecutorGroup eventExecutorGroup;
  private final PacketDispatcherHandler packetDispatcherHandler;

  private final SslContext sslContext;
  private final boolean isFlashSupported;
  private final boolean isJsonpSupported;

  private final PipelineModifier pipelineModifier;

  public SocketIOChannelInitializer(final ServerConfiguration serverConfiguration, final SocketIOListener listener, final PipelineModifier pipelineModifier) {
    // Initialize state variables
    this.sslContext = serverConfiguration.getSslContext();
    final String remoteAddressHeader = serverConfiguration.getRemoteAddressHeader();

    final SessionStorage sessionFactory = new SessionStorage(serverConfiguration.getPort());
    isFlashSupported = serverConfiguration.getTransports().contains(TransportType.FLASHSOCKET.getName());
    isJsonpSupported = serverConfiguration.getTransports().contains(TransportType.JSONP_POLLING.getName());

    // Initialize sharable handlers
    flashPolicyHandler = new FlashPolicyHandler();

    flashResourceHandler = new ResourceHandler();
    flashResourceHandler.addResource(CONTEXT_PATH + FLASH_SOCKET_RESOURCE_PATH, FLASH_SOCKET_RESOURCE_PATH);
    flashResourceHandler.addResource(CONTEXT_PATH + FLASH_SOCKET_INSECURE_RESOURCE_PATH, FLASH_SOCKET_INSECURE_RESOURCE_PATH);

    packetEncoderHandler = new PacketEncoderHandler();

    handshakeHandler = new HandshakeHandler(HANDSHAKE_PATH, serverConfiguration.getHeartbeatTimeout(), serverConfiguration.getCloseTimeout(), serverConfiguration.getTransports());
    disconnectHandler = new DisconnectHandler();
    heartbeatHandler = new HeartbeatHandler(sessionFactory);

    final boolean secure = (sslContext != null) || serverConfiguration.isAlwaysSecureWebSocketLocation();
    final int maxWebSocketFrameSize = serverConfiguration.getMaxWebSocketFrameSize();
    webSocketHandler = new WebSocketHandler(HANDSHAKE_PATH, secure, maxWebSocketFrameSize, remoteAddressHeader);
    flashSocketHandler = new FlashSocketHandler(HANDSHAKE_PATH, secure, maxWebSocketFrameSize, remoteAddressHeader);

    xhrPollingHandler = new XHRPollingHandler(HANDSHAKE_PATH, remoteAddressHeader);
    jsonpPollingHandler = new JsonpPollingHandler(HANDSHAKE_PATH, remoteAddressHeader);

    packetDispatcherHandler = new PacketDispatcherHandler(sessionFactory, listener);
    if (serverConfiguration.isEventExecutorEnabled()) {
      eventExecutorGroup = new DefaultEventExecutorGroup(serverConfiguration.getEventExecutorThreadNumber());
    } else {
      eventExecutorGroup = null;
    }
    this.pipelineModifier = pipelineModifier;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    // Flash policy file
    if (isFlashSupported) {
      pipeline.addLast(FLASH_POLICY_HANDLER, flashPolicyHandler);
    }
    // SSL
    if (sslContext != null) {
      pipeline.addLast(SSL_HANDLER, sslContext.newHandler(ch.alloc()));
    }

    // HTTP
    pipeline.addLast(HTTP_REQUEST_DECODER, new HttpRequestDecoder());
    pipeline.addLast(HTTP_CHUNK_AGGREGATOR, new HttpObjectAggregator(MAX_HTTP_CONTENT_LENGTH));
    pipeline.addLast(HTTP_RESPONSE_ENCODER, new HttpResponseEncoder());

    // Flash resources
    if (isFlashSupported) {
      pipeline.addLast(FLASH_RESOURCE_HANDLER, flashResourceHandler);
    }

    // Socket.IO
    pipeline.addLast(SOCKETIO_PACKET_ENCODER, packetEncoderHandler);
    pipeline.addLast(SOCKETIO_HANDSHAKE_HANDLER, handshakeHandler);
    pipeline.addLast(SOCKETIO_DISCONNECT_HANDLER, disconnectHandler);
    pipeline.addLast(SOCKETIO_WEBSOCKET_HANDLER, webSocketHandler);
    if (isFlashSupported) {
      pipeline.addLast(SOCKETIO_FLASHSOCKET_HANDLER, flashSocketHandler);
    }
    pipeline.addLast(SOCKETIO_XHR_POLLING_HANDLER, xhrPollingHandler);
    if (isJsonpSupported) {
      pipeline.addLast(SOCKETIO_JSONP_POLLING_HANDLER, jsonpPollingHandler);
    }
    pipeline.addLast(SOCKETIO_HEARTBEAT_HANDLER, heartbeatHandler);
    pipeline.addLast(eventExecutorGroup, SOCKETIO_PACKET_DISPATCHER, packetDispatcherHandler);

    if (pipelineModifier != null) {
      pipelineModifier.modifyPipeline(pipeline);
    }
  }
}
