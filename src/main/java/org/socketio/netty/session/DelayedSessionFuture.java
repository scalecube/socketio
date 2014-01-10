package org.socketio.netty.session;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.socketio.netty.ISession;
import org.socketio.netty.ISessionFuture;
import org.socketio.netty.ISessionFutureListener;

public class DelayedSessionFuture implements ISessionFuture {
	
	private final ConcurrentLinkedQueue<ISessionFutureListener> listeners = new ConcurrentLinkedQueue<ISessionFutureListener>();
	
	private final ISession session;
	
	private AtomicReference<ISessionFuture> sessionFutureHolder = new AtomicReference<ISessionFuture>(null);
	
	public DelayedSessionFuture(ISession session) {
		this.session = session;
	}
	
	public void initSessionFuture(ISessionFuture sessionFuture) {
		if (sessionFutureHolder.compareAndSet(null, sessionFuture)) {
			ISessionFutureListener listener;
			while((listener = listeners.poll()) != null) {
				sessionFuture.addListener(listener);
			}
		} else {
			throw new IllegalStateException("Session future already initialized");
		}
	}

	@Override
	public ISession getSession() {
		return session;
	}

	@Override
	public boolean isDone() {
		if (sessionFutureHolder.get() != null) {
			return sessionFutureHolder.get().isDone();
		} else {
			return false;
		}
	}

	@Override
	public boolean isSuccess() {
		if (sessionFutureHolder.get() != null) {
			return sessionFutureHolder.get().isSuccess();
		} else {
			return false;
		}
	}

	@Override
	public Throwable getCause() {
		if (sessionFutureHolder.get() != null) {
			return sessionFutureHolder.get().getCause();
		} else {
			return null;
		}
	}

	@Override
	public void addListener(final ISessionFutureListener listener) {
		if (listener == null) {
            throw new NullPointerException("listener");
        }
		
		if (sessionFutureHolder.get() != null) {
			sessionFutureHolder.get().addListener(listener);
		} else {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(ISessionFutureListener listener) {
		if (listener == null) {
            throw new NullPointerException("listener");
        }
		
		if (sessionFutureHolder.get() != null) {
			sessionFutureHolder.get().removeListener(listener);
		} else {
			while (listeners.remove(listener));
		}
	}
}
