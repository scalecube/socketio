package org.socketio.netty;

public class SocketIOServerConfiguration {
    int port = 8080;
    int heartbeatThreadpoolSize = 5;
    int heartbeatTimeout = 30;
    int heartbeatInterval = 20;
    int closeTimeout = 25;
    String transports = "websocket,flashsocket,xhr-polling,jsonp-polling";
    boolean alwaysSecureWebSocketLocation = false;
    String headerClientIpAddressName;
    boolean eventExecutorEnabled = true;
    int eventWorkersNumber = Runtime.getRuntime().availableProcessors();
    

    SocketIOServerConfiguration() {
    }

    void setPort(int port) {
        this.port = port;
    }

    void setHeartbeatThreadpoolSize(int heartbeatThreadpoolSize) {
        this.heartbeatThreadpoolSize = heartbeatThreadpoolSize;
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

    /**
     * Port on which Socket.IO server will be started. Default value is 8080.
     */
    public int getPort() {
        return port;
    }

    /**
     * gets heartbeat thread pool size. This parameter can be used for
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

    public int getEventWorkersNumber() {
        return eventWorkersNumber;
    }



    public boolean isEventExecutorEnabled() {
        return eventExecutorEnabled;
    }

    public static class Builder{
        private final SocketIOServerConfiguration configuration = new SocketIOServerConfiguration();

        /**
         * Port on which Socket.IO server will be started. Default value is 8080.
         */
        public Builder setPort(int port){
            configuration.port = port;
            return this;
        }

        /**
         * sets heartbeat thread pool size. This parameter can be used for
         * fine-tuning heartbeat scheduler performance.
         */
        public Builder setHeartbeatThreadpoolSize(int heartbeatThreadpoolSize) {
            configuration.setHeartbeatThreadpoolSize(heartbeatThreadpoolSize);
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

        /**
         * Return server configuration
         * @return server configuration
         */
        public SocketIOServerConfiguration build(){
            return configuration;
        }

    }
}