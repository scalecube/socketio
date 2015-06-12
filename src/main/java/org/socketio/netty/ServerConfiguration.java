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
package org.socketio.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Class represents different options of socket.io server
 */

public class ServerConfiguration {

    // Default parameters
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_HEARTBEAT_TIMEOUT = 60;
    private static final int DEFAULT_HEARTBEAT_INTERVAL = 25;
    private static final int DEFAULT_CLOSE_TIMEOUT = 60;


    private int port = DEFAULT_PORT;
    private int heartbeatTimeout = DEFAULT_HEARTBEAT_TIMEOUT;
    private int heartbeatInterval = DEFAULT_HEARTBEAT_INTERVAL;
    private int closeTimeout = DEFAULT_CLOSE_TIMEOUT;
    private String transports = "websocket,flashsocket,xhr-polling,jsonp-polling";
    private boolean alwaysSecureWebSocketLocation = false;
    private String headerClientIpAddressName;
    private boolean eventExecutorEnabled = true;
    private int eventWorkersNumber = Runtime.getRuntime().availableProcessors();
	private ServerBootstrap bootstrap;

	/**
	 * Private constructor. Use {@link org.socketio.netty.ServerConfiguration.Builder} to build configuration.
	 */
	ServerConfiguration() {
		this(new NioEventLoopGroup(), new NioEventLoopGroup());
	}

	ServerConfiguration(NioEventLoopGroup parentGroup, NioEventLoopGroup childGroup) {
		bootstrap = new ServerBootstrap()
				.group(parentGroup, childGroup)
				.channel(NioServerSocketChannel.class)
				.childOption(ChannelOption.TCP_NODELAY, true)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
	}

    void setPort(int port) {
        this.port = port;
    }

    void setHeartbeatTimeout(int heartbeatTimeout) {
        this.heartbeatTimeout = heartbeatTimeout;
    }

    void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    void setCloseTimeout(int closeTimeout) {
        this.closeTimeout = closeTimeout;
    }

    void setTransports(String transports) {
        this.transports = transports;
    }

    void setAlwaysSecureWebSocketLocation(boolean alwaysSecureWebSocketLocation) {
        this.alwaysSecureWebSocketLocation = alwaysSecureWebSocketLocation;
    }

    void setHeaderClientIpAddressName(String headerClientIpAddressName) {
        this.headerClientIpAddressName = headerClientIpAddressName;
    }

    void setEventExecutorEnabled(boolean eventExecutorEnabled) {
        this.eventExecutorEnabled = eventExecutorEnabled;
    }

    void setEventWorkersNumber(int eventWorkersNumber) {
        this.eventWorkersNumber = eventWorkersNumber;
    }

	public void setBootstrap(ServerBootstrap bootstrap) {
		this.bootstrap = bootstrap;
	}

    /**
     * Port on which Socket.IO server will be started. Default value is 8080.
     */
    public int getPort() {
        return port;
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

	public ServerBootstrap getBootstrap() {
		return bootstrap;
	}

    public boolean isEventExecutorEnabled() {
        return eventExecutorEnabled;
    }

	public static class Builder {
		private final ServerConfiguration configuration;

		public Builder() {
			configuration = new ServerConfiguration();
		}

		public Builder(NioEventLoopGroup parentGroup, NioEventLoopGroup childGroup) {
			configuration = new ServerConfiguration(parentGroup, childGroup);
		}

		/**
		 * Port on which Socket.IO server will be started. Default value is 8080.
		 */
        public Builder setPort(int port){
            configuration.setPort(port);
            return this;
        }

        /**
         * The timeout in seconds for the client when it should send a new heart
         * beat to the server. This value is sent to the client after a successful
         * handshake. The default value is 30.
         */
        public Builder setHeartbeatTimeout(int heartbeatTimeout) {
            configuration.setHeartbeatTimeout(heartbeatTimeout);
            return this;
        }

        /**
         * The timeout in seconds for the server, we should receive a heartbeat from
         * the client within this interval. This should be less than the heartbeat
         * timeout. Default value is 20.
         */
        public Builder setHeartbeatInterval(int heartbeatInterval) {
            configuration.setHeartbeatInterval(heartbeatInterval);
            return this;
        }

        /**
         * The timeout in seconds for the client, when it closes the connection it
         * still X amounts of seconds to do re open of the connection. This value is
         * sent to the client after a successful handshake. Default value is 25.
         */
        public Builder setCloseTimeout(int closeTimeout) {
            configuration.setCloseTimeout(closeTimeout);
            return this;
        }

        /**
         * A string with list of allowed transport methods separated by comma.
         * Default value is "websocket,xhr-polling".
         */
        public Builder setTransports(String transports) {
            configuration.setTransports(transports);
            return this;
        }

        public Builder setAlwaysSecureWebSocketLocation(boolean alwaysSecureWebSocketLocation) {
            configuration.setAlwaysSecureWebSocketLocation(alwaysSecureWebSocketLocation);
            return this;
        }

        public Builder setHeaderClientIpAddressName(String headerClientIpAddressName) {
            configuration.setHeaderClientIpAddressName(headerClientIpAddressName);
            return this;
        }

        /**
         * Sets flag if listener will be executed, true - different thread, false - io-thread.
         * Default is true
         * @param eventExecutorEnabled true - listener will be executed in different thread, false - in io thread.
         * @return this
         */
        public Builder setEventExecutorEnabled(boolean eventExecutorEnabled) {
            configuration.setEventExecutorEnabled(eventExecutorEnabled);
            return this;
        }

        /**
         * Set worker executor number, if eventExecutorEnabled is enabled.
         * Default value is Runtime.getRuntime().availableProcessors()
         * @param eventWorkersNumber
         * @return this
         */
        public Builder setEventWorkersNumber(int eventWorkersNumber) {
            configuration.setEventWorkersNumber(eventWorkersNumber);
            return this;
        }

		public Builder setBootstrap(ServerBootstrap bootstrap) {
			configuration.setBootstrap(bootstrap);
			return this;
		}

        /**
         * Return server configuration
         * @return server configuration
         */
        public ServerConfiguration build(){
            return configuration;
        }

    }
}