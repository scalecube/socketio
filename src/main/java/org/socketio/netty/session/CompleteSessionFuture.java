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
