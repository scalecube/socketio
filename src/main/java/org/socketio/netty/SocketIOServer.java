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
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.pipeline.SocketIOChannelInitializer;
import org.socketio.netty.session.SocketIOHeartbeatScheduler;

/**
 * A Socket.IO server launcher class.
 * 
 * @author Anton Kharenko
 */
public class SocketIOServer {

    private final SocketIOServerConfiguration socketIOServerConfiguration;

    private enum State {
		STARTED, STOPPED
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	private volatile State state = State.STOPPED;

	private ServerBootstrap bootstrap;

	private ScheduledExecutorService heartbeatScheduller;

	private ISocketIOListener listener;

    private SSLContext sslContext = null;

    /**
	 * Creates Socket.IO server with default settings.
	 */
	public SocketIOServer() {
       this(new SocketIOServerConfiguration());
	}


    public SocketIOServer(SocketIOServerConfiguration configuration){
        this.socketIOServerConfiguration = configuration;
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
		heartbeatScheduller = Executors.newScheduledThreadPool(socketIOServerConfiguration.getHeartbeatThreadpoolSize());
		SocketIOHeartbeatScheduler.setScheduledExecutorService(heartbeatScheduller);
		SocketIOHeartbeatScheduler.setHeartbeatInterval(socketIOServerConfiguration.getHeartbeatInterval());

		// Configure server
        SocketIOChannelInitializer channelInitializer = new SocketIOChannelInitializer(
                socketIOServerConfiguration, listener,
                sslContext
        );
		bootstrap = new ServerBootstrap()
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
				.childHandler(channelInitializer)
                .childOption(ChannelOption.TCP_NODELAY, true);

		int port = socketIOServerConfiguration.getPort();
		bootstrap.bind(new InetSocketAddress(port));

		log.info("Started {}", this);

		state = State.STARTED;
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

		heartbeatScheduller.shutdown();
		bootstrap.group().shutdownGracefully();

		log.info("Socket.IO server stopped");

		state = State.STOPPED;
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
	public ISocketIOListener getListener() {
		return listener;
	}

	/**
	 * {@link SocketIOServer#getListener}
	 */
	public void setListener(ISocketIOListener listener) {
		this.listener = listener;
	}

	/**
	 * Port on which Socket.IO server will be started. Default value is 8080.
	 */
	public int getPort() {
        return socketIOServerConfiguration.getPort();
    }

	/**
	 * {@link SocketIOServer#getPort}
	 */
	public void setPort(int port) {
        this.socketIOServerConfiguration.port = port;
	}

	/**
	 * Sets heartbeat thread pool size. This parameter can be used for
	 * fine-tuning heartbeat scheduler performance.
	 */
	public int getHeartbeatThreadpoolSize() {
        return socketIOServerConfiguration.getHeartbeatThreadpoolSize();
    }

	/**
	 * {@link SocketIOServer#getListener}
	 */
	public void setHeartbeatThreadpoolSize(int heartbeatThreadpoolSize) {
        this.socketIOServerConfiguration.heartbeatThreadpoolSize = heartbeatThreadpoolSize;
	}

	/**
	 * The timeout in seconds for the client when it should send a new heart
	 * beat to the server. This value is sent to the client after a successful
	 * handshake. The default value is 30.
	 */
	public int getHeartbeatTimeout() {
        return socketIOServerConfiguration.getHeartbeatTimeout();
    }

	/**
	 * {@link SocketIOServer#getHeartbeatTimeout}
	 */
	public void setHeartbeatTimeout(int heartbeatTimeout) {
        this.socketIOServerConfiguration.heartbeatTimeout = heartbeatTimeout;
	}

	/**
	 * The timeout in seconds for the client, when it closes the connection it
	 * still X amounts of seconds to do re open of the connection. This value is
	 * sent to the client after a successful handshake. Default value is 25.
	 */
	public int getCloseTimeout() {
        return socketIOServerConfiguration.getCloseTimeout();
    }

	/**
	 * {@link SocketIOServer#getCloseTimeout}
	 */
	public void setCloseTimeout(int closeTimeout) {
        this.socketIOServerConfiguration.closeTimeout = closeTimeout;
	}

	/**
	 * A string with list of allowed transport methods separated by comma.
	 * Default value is "websocket,xhr-polling".
	 */
	public String getTransports() {
        return socketIOServerConfiguration.getTransports();
    }

	/**
	 * {@link SocketIOServer#getTransports}
	 */
	public void setTransports(String transports) {
        this.socketIOServerConfiguration.transports = transports;
	}

	/**
	 * The timeout in seconds for the server, we should receive a heartbeat from
	 * the client within this interval. This should be less than the heartbeat
	 * timeout. Default value is 20.
	 */
	public int getHeartbeatInterval() {
        return socketIOServerConfiguration.getHeartbeatInterval();
    }

	/**
	 * {@link SocketIOServer#getHeartbeatInterval}
	 */
	public void setHeartbeatInterval(int heartbeatInterval) {
        this.socketIOServerConfiguration.heartbeatInterval = heartbeatInterval;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	public boolean isAlwaysSecureWebSocketLocation() {
        return socketIOServerConfiguration.isAlwaysSecureWebSocketLocation();
    }

	public void setAlwaysSecureWebSocketLocation(boolean alwaysSecureWebSocketLocation) {
        this.socketIOServerConfiguration.alwaysSecureWebSocketLocation = alwaysSecureWebSocketLocation;
	}

	/**
	 * @return the headerClientIpAddressName
	 */
	public String getHeaderClientIpAddressName() {
        return socketIOServerConfiguration.getHeaderClientIpAddressName();
    }

	/**
	 * @param headerClientIpAddressName the headerClientIpAddressName to set
	 */
	public void setHeaderClientIpAddressName(String headerClientIpAddressName) {
        this.socketIOServerConfiguration.headerClientIpAddressName = headerClientIpAddressName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SocketIOServer [port=");
		builder.append(socketIOServerConfiguration.getPort());
		builder.append(", heartbeatThreadpoolSize=");
		builder.append(socketIOServerConfiguration.getHeartbeatThreadpoolSize());
		builder.append(", heartbeatTimeout=");
		builder.append(socketIOServerConfiguration.getHeartbeatTimeout());
		builder.append(", heartbeatInterval=");
		builder.append(socketIOServerConfiguration.getHeartbeatInterval());
		builder.append(", closeTimeout=");
		builder.append(socketIOServerConfiguration.getCloseTimeout());
		builder.append(", transports=");
		builder.append(socketIOServerConfiguration.getTransports());
		builder.append(", ssl=");
		builder.append(sslContext != null);
		builder.append(", alwaysSecureWebSocketLocation=");
		builder.append(socketIOServerConfiguration.isAlwaysSecureWebSocketLocation());
		builder.append(", headerClientIpAddressName=");
		builder.append(socketIOServerConfiguration.getHeaderClientIpAddressName());
		builder.append("]");
		return builder.toString();
	}

}
