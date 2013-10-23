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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SocketIOHeartbeatScheduler {
	
	private static int heartbeatInterval;
	
	private static ScheduledExecutorService executorService;
	
	private final IInternalSession session;
	
	private ScheduledFuture<?> future = null;
	
	private volatile boolean disabled = false;
	
	public SocketIOHeartbeatScheduler(final IInternalSession session) {
		this.session = session;
	}

	public static void setScheduledExecutorService(final ScheduledExecutorService executorService) {
		SocketIOHeartbeatScheduler.executorService = executorService;
	}
	
	public static void setHeartbeatInterval(int heartbeatInterval) {
		SocketIOHeartbeatScheduler.heartbeatInterval = heartbeatInterval;
	}

	public synchronized void reschedule() {
		if (!disabled) {
			cancelHeartbeat();
			scheduleHeartbeat();
		}
	}
	
	private void cancelHeartbeat() {
		if (future != null && !future.isCancelled()) {
			future.cancel(true);
		}
	}
	
	public void disableHeartbeat() {
		disabled = true;
	}
	
	private void scheduleHeartbeat() {
		future = executorService.schedule(new Runnable() {
			@Override
			public void run() {
				if (!disabled) {
					session.sendHeartbeat();
					scheduleDisconnect();
				}
			}
		}, heartbeatInterval, TimeUnit.SECONDS);
	}
	
	public synchronized void scheduleDisconnect() {
		future = executorService.schedule(new Runnable() {
			@Override
			public void run() {
				if (!disabled) {
					session.disconnect();
				}
			}
		}, heartbeatInterval, TimeUnit.SECONDS);
	}
}
