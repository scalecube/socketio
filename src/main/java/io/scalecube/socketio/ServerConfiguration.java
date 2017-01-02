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

import javax.net.ssl.SSLContext;

/**
 * Class represents different options of socket.io server
 */
public final class ServerConfiguration {

  // Default configuration
  public static final ServerConfiguration DEFAULT = builder().build();

  // Default settings
  public static final int DEFAULT_PORT = 8080;
  public static final int DEFAULT_HEARTBEAT_TIMEOUT = 60;
  public static final int DEFAULT_HEARTBEAT_INTERVAL = 25;
  public static final int DEFAULT_CLOSE_TIMEOUT = 60;
  public static final String DEFAULT_TRANSPORTS = "websocket,flashsocket,xhr-polling,jsonp-polling";
  public static final boolean DEFAULT_ALWAYS_SECURE_WEB_SOCKET_LOCATION = false;
  public static final String DEFAULT_REMOTE_ADDRESS_HEADER = null;
  public static final boolean DEFAULT_EVENT_EXECUTOR_ENABLED = true;
  public static final int DEFAULT_EVENT_EXECUTOR_THREAD_NUMBER = Runtime.getRuntime().availableProcessors() * 2;
  public static final int DEFAULT_MAX_WEB_SOCKET_FRAME_SIZE = 65536;
  public static final SSLContext DEFAULT_SSL_CONTEXT = null;
  public static final boolean DEFAULT_EPOLL_ENABLED = true;

  private final int port;
  private final int heartbeatTimeout;
  private final int heartbeatInterval;
  private final int closeTimeout;
  private final String transports;
  private final boolean alwaysSecureWebSocketLocation;
  private final String remoteAddressHeader;
  private final boolean eventExecutorEnabled;
  private final int eventExecutorThreadNumber;
  private final int maxWebSocketFrameSize;
  private final SSLContext sslContext;
  private final boolean epollEnabled;

  /**
   * Private constructor. Use {@link ServerConfiguration.Builder} to build configuration.
   */
  private ServerConfiguration(Builder builder) {
    this.port = builder.port;
    this.heartbeatTimeout = builder.heartbeatTimeout;
    this.heartbeatInterval = builder.heartbeatInterval;
    this.closeTimeout = builder.closeTimeout;
    this.transports = builder.transports;
    this.alwaysSecureWebSocketLocation = builder.alwaysSecureWebSocketLocation;
    this.remoteAddressHeader = builder.remoteAddressHeader;
    this.eventExecutorEnabled = builder.eventExecutorEnabled;
    this.eventExecutorThreadNumber = builder.eventExecutorThreadNumber;
    this.maxWebSocketFrameSize = builder.maxWebSocketFrameSize;
    this.sslContext = builder.sslContext;
    this.epollEnabled = builder.epollEnabled;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Port on which Socket.IO server will be started. Default value is 8080.
   */
  public int getPort() {
    return port;
  }

  /**
   * SSL context which is used to run secure socket. If it's set to null server runs without SSL.
   * Default value is null.
   */
  public SSLContext getSslContext() {
    return sslContext;
  }

  /**
   * The timeout in seconds for the client when it should send a new heart
   * beat to the server. This value is sent to the client after a successful
   * handshake. The default value is 60.
   */
  public int getHeartbeatTimeout() {
    return heartbeatTimeout;
  }

  /**
   * The timeout in seconds for the client, when it closes the connection it
   * still X amounts of seconds to do re open of the connection. This value is
   * sent to the client after a successful handshake. Default value is 60.
   */
  public int getCloseTimeout() {
    return closeTimeout;
  }

  /**
   * A string with list of allowed transport methods separated by comma.
   * Default value is "websocket,flashsocket,xhr-polling,jsonp-polling".
   */
  public String getTransports() {
    return transports;
  }

  /**
   * The timeout in seconds for the server, we should receive a heartbeat from
   * the client within this interval. This should be less than the heartbeat
   * timeout. Default value is 25.
   */
  public int getHeartbeatInterval() {
    return heartbeatInterval;
  }

  /**
   * Flag which if set to true will always return secure web socket location protocol ("wss://")
   * even when connection is established over plain socket. It is used as a workaround related to case
   * when SSL is offloaded to Load Balancer, but it doesn't modify web socket location. By default it
   * is false.
   */
  public boolean isAlwaysSecureWebSocketLocation() {
    return alwaysSecureWebSocketLocation;
  }

  /**
   * The HTTP header name which is used as a session remote address. It is a workaround related to case
   * when Load Balancer modify client address with its address. This header is supposed to be set by Load
   * Balancer. If it is set to null then this header is not used. Default value is null.
   */
  public String getRemoteAddressHeader() {
    return remoteAddressHeader;
  }

  /**
   * Flag which defines if listener will be executed, true - different thread, false - io-thread.
   * Default is true.
   */
  public boolean isEventExecutorEnabled() {
    return eventExecutorEnabled;
  }

  /*
   * Event executor thread number, if eventExecutorEnabled flag set to true.
   * Default value is Runtime.getRuntime().availableProcessors() x 2.
   */
  public int getEventExecutorThreadNumber() {
    return eventExecutorThreadNumber;
  }

  /**
   * Maximum allowable web socket frame payload length. Setting this value to your application's requirement may
   * reduce denial of service attacks using long data frames. Default is 65536.
   */
  public int getMaxWebSocketFrameSize() {
    return maxWebSocketFrameSize;
  }

  /**
   * Flag which defines if Linux native epoll transport will be used if available. Default is true.
   */
  public boolean isEpollEnabled() {
    return epollEnabled;
  }

  @Override
  public String toString() {
    return "ServerConfiguration{port=" + port +
        ", ssl=" + (sslContext != null) +
        ", heartbeatTimeout=" + heartbeatTimeout +
        ", heartbeatInterval=" + heartbeatInterval +
        ", closeTimeout=" + closeTimeout +
        ", transports='" + transports + '\'' +
        ", alwaysSecureWebSocketLocation=" + alwaysSecureWebSocketLocation +
        ", remoteAddressHeader=" + remoteAddressHeader +
        ", eventExecutorEnabled=" + eventExecutorEnabled +
        ", eventExecutorThreadNumber=" + eventExecutorThreadNumber +
        ", maxWebSocketFrameSize=" + maxWebSocketFrameSize +
        '}';
  }

  public static class Builder {

    private int port = DEFAULT_PORT;
    private int heartbeatTimeout = DEFAULT_HEARTBEAT_TIMEOUT;
    private int heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
    private int closeTimeout = DEFAULT_CLOSE_TIMEOUT;
    private String transports = DEFAULT_TRANSPORTS;
    private boolean alwaysSecureWebSocketLocation = DEFAULT_ALWAYS_SECURE_WEB_SOCKET_LOCATION;
    private String remoteAddressHeader = DEFAULT_REMOTE_ADDRESS_HEADER;
    private boolean eventExecutorEnabled = DEFAULT_EVENT_EXECUTOR_ENABLED;
    private int eventExecutorThreadNumber = DEFAULT_EVENT_EXECUTOR_THREAD_NUMBER;
    private int maxWebSocketFrameSize = DEFAULT_MAX_WEB_SOCKET_FRAME_SIZE;
    private SSLContext sslContext = DEFAULT_SSL_CONTEXT;
    private boolean epollEnabled = DEFAULT_EPOLL_ENABLED;

    private Builder() {}

    /**
     * See {@link ServerConfiguration#getPort()}
     */
    public Builder port(int port) {
      this.port = port;
      return this;
    }

    /**
     * See {@link ServerConfiguration#getSslContext()}
     */
    public Builder sslContext(SSLContext sslContext) {
      this.sslContext = sslContext;
      return this;
    }

    /**
     * See {@link ServerConfiguration#getHeartbeatTimeout()}
     */
    public Builder heartbeatTimeout(int heartbeatTimeout) {
      this.heartbeatTimeout = heartbeatTimeout;
      return this;
    }

    /**
     * See {@link ServerConfiguration#getHeartbeatInterval()}
     */
    public Builder heartbeatInterval(int heartbeatInterval) {
      this.heartbeatInterval = heartbeatInterval;
      return this;
    }

    /**
     * See {@link ServerConfiguration#getCloseTimeout()}
     */
    public Builder closeTimeout(int closeTimeout) {
      this.closeTimeout = closeTimeout;
      return this;
    }

    /**
     * See {@link ServerConfiguration#getTransports()}
     */
    public Builder transports(String transports) {
      this.transports = transports;
      return this;
    }

    /**
     * See {@link ServerConfiguration#isAlwaysSecureWebSocketLocation()}
     */
    public Builder alwaysSecureWebSocketLocation(boolean alwaysSecureWebSocketLocation) {
      this.alwaysSecureWebSocketLocation = alwaysSecureWebSocketLocation;
      return this;
    }

    /**
     * See {@link ServerConfiguration#getRemoteAddressHeader()}
     */
    public Builder remoteAddressHeader(String remoteAddressHeader) {
      this.remoteAddressHeader = remoteAddressHeader;
      return this;
    }

    /**
     * See {@link ServerConfiguration#getMaxWebSocketFrameSize()}
     */
    public Builder maxWebSocketFrameSize(int maxWebSocketFrameSize) {
      this.maxWebSocketFrameSize = maxWebSocketFrameSize;
      return this;
    }

    /**
     * See {@link ServerConfiguration#isEventExecutorEnabled()}
     */
    public Builder eventExecutorEnabled(boolean eventExecutorEnabled) {
      this.eventExecutorEnabled = eventExecutorEnabled;
      return this;
    }

    /**
     * See {@link ServerConfiguration#getEventExecutorThreadNumber()}
     */
    public Builder eventExecutorThreadNumber(int eventExecutorThreadNumber) {
      this.eventExecutorThreadNumber = eventExecutorThreadNumber;
      return this;
    }

    /**
     * See {@link ServerConfiguration#isEpollEnabled()}
     */
    public Builder epollEnabled(boolean epollEnabled) {
      this.epollEnabled = epollEnabled;
      return this;
    }

    /**
     * Creates new instance of {@code ServerConfiguration}
     */
    public ServerConfiguration build() {
      return new ServerConfiguration(this);
    }

  }
}
