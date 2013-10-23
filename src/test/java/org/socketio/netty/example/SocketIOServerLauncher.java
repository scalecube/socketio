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
package org.socketio.netty.example;

import org.socketio.netty.SocketIOServer;

public class SocketIOServerLauncher {
	
	// Client content server configuration
	private static final int CLIENT_PORT = 9001;
	private static final int CLIENT_OVER_SSL_PORT = 9002;
	private static final String CLIENT_PATH = "/client";
	
	// Socket.IO server configuration
	private static final int SOCKETIO_PORT = 4810;
	private static final int SOCKETIO_OVER_SSL_PORT = 9992;
	private static final String SOCKETIO_TRANSPORTS = "websocket,xhr-polling";
	
	public static void main(String[] args) {
		// Start HTTP server with socket.io demo client
		SocketIOClientContentServer clientContentServer = new SocketIOClientContentServer(
				CLIENT_PORT, CLIENT_PATH);
		clientContentServer.start();
		
		// Start HTTP over SSL server with socket.io demo client
		SocketIOClientContentServer clientContentOverSSLServer = new SocketIOClientContentServer(
				CLIENT_OVER_SSL_PORT, CLIENT_PATH, SecureSslContextFactory.getServerContext());
		clientContentOverSSLServer.start();
		
		// Start Socket.IO server
		SocketIOServer socketioServer = new SocketIOServer();
		socketioServer.setPort(SOCKETIO_PORT);
		socketioServer.setTransports(SOCKETIO_TRANSPORTS);
		socketioServer.setListener(new EchoSocketIOListener());
		socketioServer.start();
		
		// Start Socket.IO over SSL server
		SocketIOServer socketioOverSSLServer = new SocketIOServer();
		socketioOverSSLServer.setPort(SOCKETIO_OVER_SSL_PORT);
		socketioOverSSLServer.setTransports(SOCKETIO_TRANSPORTS);
		socketioOverSSLServer.setSslContext(SecureSslContextFactory.getServerContext());
		socketioOverSSLServer.setListener(new EchoSocketIOListener());
		socketioOverSSLServer.start();
	}
}
