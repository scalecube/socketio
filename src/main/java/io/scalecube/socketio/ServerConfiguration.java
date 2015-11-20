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
public class ServerConfiguration {

  // Default configuration
  public static final ServerConfiguration DEFAULT = builder().build();

  // Default settings
  public static final int DEFAULT_PORT = 8080;
  public static final int DEFAULT_HEARTBEAT_TIMEOUT = 60;
  public static final int DEFAULT_HEARTBEAT_INTERVAL = 25;
  public static final int DEFAULT_CLOSE_TIMEOUT = 60;
  public static final String DEFAULT_TRANSPORTS = "websocket,flashsocket,xhr-polling,jsonp-polling";
  public static final boolean DEFAULT_ALWAYS_SECURE_WEB_SOCKET_LOCATION = false;
  public static final String DEFAULT_HEADER_CLIENT_IP_ADDRESS_NAME = null;
  public static final boolean DEFAULT_EVENT_EXECUTORS_ENABLED = true;
  public static final int DEFAULT_EVENT_WORKERS_NUMBER = Runtime.getRuntime().availableProcessors();
  public static final int DEFAULT_MAX_WEB_SOCKET_FRAME_SIZE = 65536;
  public static final SSLContext DEFAULT_SSL_CONTEXT = null;

  private final int port;
  private final int heartbeatTimeout;
  private final int heartbeatInterval;
  private final int closeTimeout;
  private final String transports;
  private final boolean alwaysSecureWebSocketLocation;
  private final String headerClientIpAddressName;
  private final boolean eventExecutorEnabled;
  private final int eventWorkersNumber;
  private final int maxWebSocketFrameSize;
  private final SSLContext sslContext;

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
    this.headerClientIpAddressName = builder.headerClientIpAddressName;
    this.eventExecutorEnabled = builder.eventExecutorEnabled;
    this.eventWorkersNumber = builder.eventWorkersNumber;
    this.maxWebSocketFrameSize = builder.maxWebSocketFrameSize;
    this.sslContext = builder.sslContext;
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

  public SSLContext getSslContext() {
    return sslContext;
  }

  /**
   * The timeout in seconds for the client when it should send a new heart
   * beat to the server. This value is sent to the client after a successful
   * handshake. The default value is 30.
   */
  public int getHeartbeatTimeout() {
    return heartbeatTimeout;
  }

  /**
   * The timeout in seconds for the client, when it closes the connection it
   * still X amounts of seconds to do re open of the connection. This value is
   * sent to the client after a successful handshake. Default value is 25.
   */
  public int getCloseTimeout() {
    return closeTimeout;
  }

  /**
   * A string with list of allowed transport methods separated by comma.
   * Default value is "websocket,xhr-polling".
   */
  public String getTransports() {
    return transports;
  }

  /**
   * The timeout in seconds for the server, we should receive a heartbeat from
   * the client within this interval. This should be less than the heartbeat
   * timeout. Default value is 20.
   */
  public int getHeartbeatInterval() {
    return heartbeatInterval;
  }

  public boolean isAlwaysSecureWebSocketLocation() {
    return alwaysSecureWebSocketLocation;
  }

  /**
   * @return the headerClientIpAddressName
   */
  public String getHeaderClientIpAddressName() {
    return headerClientIpAddressName;
  }

  public int getEventWorkersNumber() {
    return eventWorkersNumber;
  }

  public boolean isEventExecutorEnabled() {
    return eventExecutorEnabled;
  }

  /**
   * Maximum allowable web socket frame payload length. Setting this value to your application's requirement may
   * reduce denial of service attacks using long data frames.
   */
  public int getMaxWebSocketFrameSize() {
    return maxWebSocketFrameSize;
  }

  @Override
  public String toString() {
    return "ServerConfiguration{" +
        "port=" + port +
        ", ssl=" + (sslContext != null) +
        ", heartbeatTimeout=" + heartbeatTimeout +
        ", heartbeatInterval=" + heartbeatInterval +
        ", closeTimeout=" + closeTimeout +
        ", transports='" + transports + '\'' +
        ", alwaysSecureWebSocketLocation=" + alwaysSecureWebSocketLocation +
        ", headerClientIpAddressName='" + headerClientIpAddressName + '\'' +
        ", eventExecutorEnabled=" + eventExecutorEnabled +
        ", eventWorkersNumber=" + eventWorkersNumber +
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
    private String headerClientIpAddressName = DEFAULT_HEADER_CLIENT_IP_ADDRESS_NAME;
    private boolean eventExecutorEnabled = DEFAULT_EVENT_EXECUTORS_ENABLED;
    private int eventWorkersNumber = DEFAULT_EVENT_WORKERS_NUMBER;
    private int maxWebSocketFrameSize = DEFAULT_MAX_WEB_SOCKET_FRAME_SIZE;
    private SSLContext sslContext = DEFAULT_SSL_CONTEXT;

    private Builder() {}

    /**
     * Port on which Socket.IO server will be started. Default value is 8080.
     */
    public Builder port(int port) {
      this.port = port;
      return this;
    }

    /**
     * SSL context in order to run on secure port. Default value is null.
     */
    public Builder sslContext(SSLContext sslContext) {
      this.sslContext = sslContext;
      return this;
    }

    /**
     * The timeout in seconds for the client when it should send a new heart
     * beat to the server. This value is sent to the client after a successful
     * handshake. The default value is 30.
     */
    public Builder heartbeatTimeout(int heartbeatTimeout) {
      this.heartbeatTimeout = heartbeatTimeout;
      return this;
    }

    /**
     * The timeout in seconds for the server, we should receive a heartbeat from
     * the client within this interval. This should be less than the heartbeat
     * timeout. Default value is 20.
     */
    public Builder setHeartbeatInterval(int heartbeatInterval) {
      this.heartbeatInterval = heartbeatInterval;
      return this;
    }

    /**
     * The timeout in seconds for the client, when it closes the connection it
     * still X amounts of seconds to do re open of the connection. This value is
     * sent to the client after a successful handshake. Default value is 25.
     */
    public Builder setCloseTimeout(int closeTimeout) {
      this.closeTimeout = closeTimeout;
      return this;
    }

    /**
     * A string with list of allowed transport methods separated by comma.
     * Default value is "websocket,xhr-polling".
     */
    public Builder setTransports(String transports) {
      this.transports = transports;
      return this;
    }

    public Builder alwaysSecureWebSocketLocation(boolean alwaysSecureWebSocketLocation) {
      this.alwaysSecureWebSocketLocation = alwaysSecureWebSocketLocation;
      return this;
    }

    public Builder headerClientIpAddressName(String headerClientIpAddressName) {
      this.headerClientIpAddressName = headerClientIpAddressName;
      return this;
    }

    public Builder headerClientIpAddressName(int maxWebSocketFrameSize) {
      this.maxWebSocketFrameSize = maxWebSocketFrameSize;
      return this;
    }

    /**
     * Sets flag if listener will be executed, true - different thread, false - io-thread.
     * Default is true
     * @param eventExecutorEnabled true - listener will be executed in different thread, false - in io thread.
     * @return this
     */
    public Builder eventExecutorEnabled(boolean eventExecutorEnabled) {
      this.eventExecutorEnabled = eventExecutorEnabled;
      return this;
    }

    /**
     * Set worker executor number, if eventExecutorEnabled is enabled.
     * Default value is Runtime.getRuntime().availableProcessors()
     * @param eventWorkersNumber worker executor number
     * @return this
     */
    public Builder eventWorkersNumber(int eventWorkersNumber) {
      this.eventWorkersNumber = eventWorkersNumber;
      return this;
    }

    public ServerConfiguration build() {
      return new ServerConfiguration(this);
    }

  }
}
