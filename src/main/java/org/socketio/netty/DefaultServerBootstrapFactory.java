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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.concurrent.ThreadFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.SystemPropertyUtil;

/**
 * @author Anton Kharenko
 */
final class DefaultServerBootstrapFactory implements ServerBootstrapFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerBootstrapFactory.class);

	private static final int BOSS_THREAD_NUM = 2;
	private static final int IO_THREAD_NUM = 0; // Netty default

	private static boolean envSupportEpoll;

	static {
		String name = SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim();
		if (!name.contains("linux")) {
			envSupportEpoll = false;
			LOGGER.warn("Env doesn't support epoll transport");
		} else {
			try {
				Class.forName("io.netty.channel.epoll.Native");
				envSupportEpoll = true;
				LOGGER.info("Use epoll transport");
			} catch (Throwable t) {
				LOGGER.warn(
						"Tried to use epoll transport, but it's not supported by host OS (or no corresponding libs included) "
								+ "using NIO instead, cause: ",
						t);
				envSupportEpoll = false;
			}
		}
	}

	private final boolean epollEnabled;

	public DefaultServerBootstrapFactory(boolean epollEnabled) {
		this.epollEnabled = epollEnabled;
	}

	@Override
	public ServerBootstrap createServerBootstrap() {
		EventLoopGroup bossGroup = createEventLoopGroup(BOSS_THREAD_NUM, "socketio-boss");
		EventLoopGroup workerGroup = createEventLoopGroup(IO_THREAD_NUM, "socketio-io");
		return new ServerBootstrap().group(bossGroup, workerGroup).channel(serverChannelClass())
				.childOption(ChannelOption.TCP_NODELAY, true).childOption(ChannelOption.SO_KEEPALIVE, true)
				.childOption(ChannelOption.SO_REUSEADDR, true)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
	}

	/**
	 * @return {@link EpollEventLoopGroup} or {@link NioEventLoopGroup} object
	 *         dep on {@link #isEpollSupported()} call.
	 */
	private EventLoopGroup createEventLoopGroup(int threadNum, String poolName) {
		ThreadFactory threadFactory = new DefaultThreadFactory(poolName, true);
		return isEpollSupported() ? new EpollEventLoopGroup(threadNum, threadFactory)
				: new NioEventLoopGroup(threadNum, threadFactory);
	}

	private Class<? extends ServerSocketChannel> serverChannelClass() {
		return isEpollSupported() ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
	}

	private boolean isEpollSupported() {
		return epollEnabled && envSupportEpoll;
	}

}