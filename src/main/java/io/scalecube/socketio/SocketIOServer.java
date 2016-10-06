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
package io.scalecube.socketio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.scalecube.socketio.pipeline.SocketIOChannelInitializer;
import io.scalecube.socketio.session.SocketIOHeartbeatScheduler;

/**
 * A Socket.IO server launcher class.
 *
 * @author Anton Kharenko
 */
public class SocketIOServer {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private enum State {
    STARTED, STOPPED
  }

  private ServerConfiguration configuration;

  private SocketIOListener listener;

  private PipelineModifier pipelineModifier;

  private HashedWheelTimer timer;

  private volatile State state = State.STOPPED;

  private ServerBootstrapFactory serverBootstrapFactory;

  private ServerBootstrap bootstrap;

  private SocketIOServer(ServerConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Creates instance of Socket.IO server with default settings.
   */
  public static SocketIOServer newInstance() {
    return new SocketIOServer(ServerConfiguration.DEFAULT);
  }

  /**
   * Creates instance of Socket.IO server with the given port.
   */
  public static SocketIOServer newInstance(int port) {
    return new SocketIOServer(ServerConfiguration.builder().port(port).build());
  }

  /**
   * Creates instance of Socket.IO server with the given secure port.
   */
  public static SocketIOServer newInstance(int port, SSLContext sslContext) {
    return new SocketIOServer(ServerConfiguration.builder()
        .port(port)
        .sslContext(sslContext)
        .build());
  }

  /**
   * Creates instance of Socket.IO server with the given configuration.
   */
  public static SocketIOServer newInstance(ServerConfiguration config) {
    return new SocketIOServer(config);
  }

  /**
   * Starts Socket.IO server with current configuration settings.
   *
   * @throws IllegalStateException
   *             if server already started
   */
  public synchronized void start() {
    if (isStarted()) {
      throw new IllegalStateException("Failed to start Socket.IO server: server already started");
    }

    log.info("Socket.IO server starting");

    // Configure heartbeat scheduler
    timer = new HashedWheelTimer();
    timer.start();
    SocketIOHeartbeatScheduler.setHashedWheelTimer(timer);
    SocketIOHeartbeatScheduler.setHeartbeatInterval(configuration.getHeartbeatInterval());
    SocketIOHeartbeatScheduler.setHeartbeatTimeout(configuration.getHeartbeatTimeout());

    // Configure server
    SocketIOChannelInitializer channelInitializer = new SocketIOChannelInitializer(configuration, listener, pipelineModifier);
    bootstrap = serverBootstrapFactory != null ? serverBootstrapFactory.createServerBootstrap() :
        createDefaultServerBootstrap();
    bootstrap.childHandler(channelInitializer);

    int port = configuration.getPort();
    bootstrap.bind(new InetSocketAddress(port));
    state = State.STARTED;
    log.info("Socket.IO server started: {}", configuration);
  }

  private ServerBootstrap createDefaultServerBootstrap() {
    return new ServerBootstrap()
        .group(new NioEventLoopGroup(), new NioEventLoopGroup())
        .channel(NioServerSocketChannel.class)
        .childOption(ChannelOption.TCP_NODELAY, true)
        .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
  }

  /**
   * Stops Socket.IO server.
   *
   * @throws IllegalStateException
   *             if server already stopped
   */
  public synchronized void stop() {
    if (isStopped()) {
      throw new IllegalStateException("Failed to stop Socket.IO server: server already stopped");
    }

    log.info("Socket.IO server stopping");

    timer.stop();
    bootstrap.group().shutdownGracefully().syncUninterruptibly();
    state = State.STOPPED;

    log.info("Socket.IO server stopped");
  }

  /**
   * Restarts Socket.IO server. If server already started it stops server;
   * otherwise it just starts server.
   */
  public synchronized void restart() {
    if (isStarted()) {
      stop();
    }
    start();
  }

  /**
   * Returns if server is in started state or not.
   */
  public boolean isStarted() {
    return state == State.STARTED;
  }

  /**
   * Returns if server is in stopped state or not.
   */
  public boolean isStopped() {
    return state == State.STOPPED;
  }

  /**
   * Socket.IO events listener.
   */
  public SocketIOListener getListener() {
    return listener;
  }

  /**
   * Sets Socket.IO events listener. If server already started new listener will be applied only after
   * server restart.
   */
  public void setListener(SocketIOListener listener) {
    this.listener = listener;
  }

  /**
   * Returns pipeline modifier
   */
  public PipelineModifier getPipelineModifier() {
    return pipelineModifier;
  }

  /**
   * Sets pipeline modifier. Pipeline modifier could be used for adding handlers to channel pipeline.
   */
  public void setPipelineModifier(PipelineModifier pipelineModifier) {
    this.pipelineModifier = pipelineModifier;
  }

  /**
   * Returns server configuration settings.
   */
  public ServerConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * Sets server configuration settings. If server already started new settings will be applied only after
   * server restart.
   */
  public void setConfiguration(ServerConfiguration configuration) {
    this.configuration = configuration;
  }

  /**
   * Returns ServerBootstrap factory.
   */
  public ServerBootstrapFactory getServerBootstrapFactory() {
    return serverBootstrapFactory;
  }

  /**
   * Sets ServerBootstrap factory. If server already started new boostrap factory will be applied only after
   * server restart.
   */
  public void setServerBootstrapFactory(ServerBootstrapFactory serverBootstrapFactory) {
    this.serverBootstrapFactory = serverBootstrapFactory;
  }

}
