package org.socketio.netty;

public class ServerProperites {
    int port = 8080;
    int heartbeatThreadpoolSize = 5;
    int heartbeatTimeout = 30;
    int heartbeatInterval = 20;
    int closeTimeout = 25;
    String transports = "websocket,flashsocket,xhr-polling,jsonp-polling";
    boolean alwaysSecureWebSocketLocation = false;
    String headerClientIpAddressName;
    

    public ServerProperites() {
    }

    /**
     * Port on which Socket.IO server will be started. Default value is 8080.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets heartbeat thread pool size. This parameter can be used for
     * fine-tuning heartbeat scheduler performance.
     */
    public int getHeartbeatThreadpoolSize() {
        return heartbeatThreadpoolSize;
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
}