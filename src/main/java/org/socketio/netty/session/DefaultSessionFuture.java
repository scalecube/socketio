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
package org.socketio.netty.session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.socketio.netty.ISession;
import org.socketio.netty.ISessionFuture;
import org.socketio.netty.ISessionFutureListener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class DefaultSessionFuture implements ISessionFuture {

	private final ConcurrentMap<ISessionFutureListener, ChannelFutureListener> listenersMap = new ConcurrentHashMap<ISessionFutureListener, ChannelFutureListener>();

	private final ChannelFuture channelFuture;

	private final ISession session;

	public DefaultSessionFuture(ChannelFuture channelFuture, ISession session) {
		this.channelFuture = channelFuture;
		this.session = session;
	}

	@Override
	public ISession getSession() {
		return session;
	}

	@Override
	public boolean isDone() {
		return channelFuture.isDone();
	}

	@Override
	public boolean isSuccess() {
		return channelFuture.isSuccess();
	}

	@Override
	public Throwable getCause() {
		return channelFuture.cause();
	}

	@Override
	public void addListener(final ISessionFutureListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}

		ChannelFutureListener channelFutureListener = new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				listener.onOperationCompleted(DefaultSessionFuture.this);
			}
		};
		listenersMap.putIfAbsent(listener, channelFutureListener);

		channelFuture.addListener(listenersMap.get(listener));
	}

	@Override
	public void removeListener(ISessionFutureListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener");
		}

		ChannelFutureListener channelFutureListener = listenersMap.remove(listener);
		if (channelFutureListener != null) {
			channelFuture.removeListener(channelFutureListener);
		}
	}

}
