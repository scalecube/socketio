package org.socketio.netty.session;

import org.socketio.netty.ISession;
import org.socketio.netty.ISessionFuture;
import org.socketio.netty.ISessionFutureListener;

public class CompleteSessionFuture implements ISessionFuture {
	
	private final ISession session;
	private final boolean success;
	private final Throwable cause;

	public CompleteSessionFuture(ISession session) {
		this(session, true, null);
	}
	
	public CompleteSessionFuture(ISession session, boolean success, Throwable cause) {
		this.session = session;
		this.success = success;
		this.cause = cause;
	}

	@Override
	public ISession getSession() {
		return session;
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public boolean isSuccess() {
		return success;
	}

	@Override
	public Throwable getCause() {
		return cause;
	}

	@Override
	public void addListener(ISessionFutureListener listener) {
		listener.onOperationCompleted(this);
	}

	@Override
	public void removeListener(ISessionFutureListener listener) {
		/* Do nothing */
	}
	
}
