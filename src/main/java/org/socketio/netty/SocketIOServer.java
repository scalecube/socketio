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

import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.pipeline.SocketIOChannelInitializer;
import org.socketio.netty.session.SocketIOHeartbeatScheduler;

import io.netty.util.HashedWheelTimer;

/**
 * A Socket.IO server launcher class.
 * 
 * @author Anton Kharenko
 */
public class SocketIOServer {

    private final ServerConfiguration configuration;
    private HashedWheelTimer timer;

    private enum State {
		STARTED, STOPPED
	}

	private final Logger log = LoggerFactory.getLogger(getClass());

	private volatile State state = State.STOPPED;

    private ISocketIOListener listener;

    private SSLContext sslContext = null;

    /**
	 * Creates Socket.IO server with default settings.
	 */
	public SocketIOServer() {
       this(new ServerConfiguration());
	}


    public SocketIOServer(ServerConfiguration configuration){
        this.configuration = configuration;
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
		SocketIOChannelInitializer channelInitializer = new SocketIOChannelInitializer(configuration, listener, sslContext);
        configuration.getBootstrap().childHandler(channelInitializer);

		int port = configuration.getPort();
        configuration.getBootstrap().bind(new InetSocketAddress(port));

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

        timer.stop();
        configuration.getBootstrap().group().shutdownGracefully();

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
        return configuration.getPort();
    }

	/**
	 * {@link SocketIOServer#getPort}
	 */
	public void setPort(int port) {
        this.configuration.setPort(port);
	}

	/**
	 * The timeout in seconds for the client when it should send a new heart
	 * beat to the server. This value is sent to the client after a successful
	 * handshake. The default value is 30.
	 */
	public int getHeartbeatTimeout() {
        return configuration.getHeartbeatTimeout();
    }

    /**
     * @deprecated replaced by {@link ServerConfiguration} to configure parameter and
     * server constructor which accept configuration {@link SocketIOServer#SocketIOServer(ServerConfiguration)}
     */
    @Deprecated
	public void setHeartbeatTimeout(int heartbeatTimeout) {
        this.configuration.setHeartbeatTimeout(heartbeatTimeout);
	}

	/**
	 * The timeout in seconds for the client, when it closes the connection it
	 * still X amounts of seconds to do re open of the connection. This value is
	 * sent to the client after a successful handshake. Default value is 25.
	 */
	public int getCloseTimeout() {
        return configuration.getCloseTimeout();
    }

    /**
     * @deprecated replaced by {@link ServerConfiguration} to configure parameter and
     * server constructor which accept configuration {@link SocketIOServer#SocketIOServer(ServerConfiguration)}
     */
    @Deprecated
	public void setCloseTimeout(int closeTimeout) {
        this.configuration.setCloseTimeout(closeTimeout);
    }

	/**
	 * A string with list of allowed transport methods separated by comma.
	 * Default value is "websocket,xhr-polling".
	 */
	public String getTransports() {
        return configuration.getTransports();
    }

    /**
     * @deprecated replaced by {@link ServerConfiguration} to configure parameter and
     * server constructor which accept configuration {@link SocketIOServer#SocketIOServer(ServerConfiguration)}
     */
    @Deprecated
	public void setTransports(String transports) {
        this.configuration.setTransports(transports);
	}

	/**
	 * The timeout in seconds for the server, we should receive a heartbeat from
	 * the client within this interval. This should be less than the heartbeat
	 * timeout. Default value is 20.
	 */
	public int getHeartbeatInterval() {
        return configuration.getHeartbeatInterval();
    }

    /**
     * @deprecated replaced by {@link ServerConfiguration} to configure parameter and
     * server constructor which accept configuration {@link SocketIOServer#SocketIOServer(ServerConfiguration)}
     */
    @Deprecated
	public void setHeartbeatInterval(int heartbeatInterval) {
        this.configuration.setHeartbeatInterval(heartbeatInterval);
	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	public boolean isAlwaysSecureWebSocketLocation() {
        return configuration.isAlwaysSecureWebSocketLocation();
    }
    /**
     * @deprecated replaced by {@link ServerConfiguration} to configure parameter and
     * server constructor which accept configuration {@link SocketIOServer#SocketIOServer(ServerConfiguration)}
     */
    @Deprecated
	public void setAlwaysSecureWebSocketLocation(boolean alwaysSecureWebSocketLocation) {
        this.configuration.setAlwaysSecureWebSocketLocation(alwaysSecureWebSocketLocation);
	}

	/**
	 * @return the headerClientIpAddressName
	 */
	public String getHeaderClientIpAddressName() {
        return configuration.getHeaderClientIpAddressName();
    }

    /**
     * @deprecated replaced by {@link ServerConfiguration} to configure parameter and
     * server constructor which accept configuration {@link SocketIOServer#SocketIOServer(ServerConfiguration)}
     */
    @Deprecated
	public void setHeaderClientIpAddressName(String headerClientIpAddressName) {
        this.configuration.setHeaderClientIpAddressName(headerClientIpAddressName);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SocketIOServer [port=");
		builder.append(configuration.getPort());
		builder.append(", heartbeatTimeout=");
		builder.append(configuration.getHeartbeatTimeout());
		builder.append(", heartbeatInterval=");
		builder.append(configuration.getHeartbeatInterval());
		builder.append(", closeTimeout=");
		builder.append(configuration.getCloseTimeout());
		builder.append(", transports=");
		builder.append(configuration.getTransports());
		builder.append(", ssl=");
		builder.append(sslContext != null);
		builder.append(", alwaysSecureWebSocketLocation=");
		builder.append(configuration.isAlwaysSecureWebSocketLocation());
		builder.append(", headerClientIpAddressName=");
		builder.append(configuration.getHeaderClientIpAddressName());
		builder.append("]");
		return builder.toString();
	}

}
