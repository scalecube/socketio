package org.socketio.netty.session;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.socketio.netty.ISession;
import org.socketio.netty.ISessionFuture;
import org.socketio.netty.ISessionFutureListener;

public class DefaultSessionFuture implements ISessionFuture {
	
	private final ConcurrentMap<ISessionFutureListener, ChannelFutureListener> listenersMap = 
			new ConcurrentHashMap<ISessionFutureListener, ChannelFutureListener>();
	
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
		return channelFuture.getCause();
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
