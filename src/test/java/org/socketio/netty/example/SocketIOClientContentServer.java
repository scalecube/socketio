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

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketIOClientContentServer {
	
	private static final Logger log = LoggerFactory.getLogger(SocketIOClientContentServer.class);
	
	private ServerBootstrap bootstrap;
	
	private final int port;
	private final String appPath;
	private final SSLContext sslContext;

	public SocketIOClientContentServer(final int port, String basePath) {
		this(port, basePath, null);
	}
	
	public SocketIOClientContentServer(final int port, String basePath, SSLContext sslContext) {
		this.port = port;
		this.appPath = basePath;
		this.sslContext = sslContext;
	}

	public void start() {
		// Configure the server.
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

		// Set up the event pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = pipeline();
				
				// SSL
				if (sslContext != null) {
					SSLEngine sslEngine = sslContext.createSSLEngine();
					sslEngine.setUseClientMode(false);
					SslHandler sslHandler = new SslHandler(sslEngine);
					sslHandler.setIssueHandshake(true);
					pipeline.addLast("ssl", sslHandler);
				}
				
				// HTTP
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
				pipeline.addLast("encoder", new HttpResponseEncoder());
				
				// Web application
				pipeline.addLast("httpPage", new SimpleHttpRequestHandler(appPath));
				
				return pipeline;
			}
		});

		// Bind and start to accept incoming connections.
		InetSocketAddress addr = new InetSocketAddress(port);
		bootstrap.bind(addr);

		log.info("Socket.IO client started at port: {} on path: {}", port, appPath);
	}
	
	public void stop() {
		bootstrap.releaseExternalResources();
		log.info("Socket.IO client stopped at port: {} on path: {}", port, appPath);
	}
}
