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

import java.util.concurrent.TimeUnit;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketIOHeartbeatScheduler {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private static int heartbeatInterval;
    private static int heartbeatTimeout;
	
    private static HashedWheelTimer hashedWheelTimer;

    private Timeout hTimeout = null;
    private Timeout dTimeout = null;
	
	private final IManagedSession session;
	
	private volatile boolean disabled = false;
	
	public SocketIOHeartbeatScheduler(final IManagedSession session) {
		this.session = session;
	}


    public static void setHashedWheelTimer(HashedWheelTimer hashedWheelTimer) {
        SocketIOHeartbeatScheduler.hashedWheelTimer = hashedWheelTimer;
    }

    public static void setHeartbeatInterval(int heartbeatInterval) {
		SocketIOHeartbeatScheduler.heartbeatInterval = heartbeatInterval;
	}

    public static void setHeartbeatTimeout(int heartbeatTimeout) {
        SocketIOHeartbeatScheduler.heartbeatTimeout = heartbeatTimeout;
    }

    public synchronized void reschedule() {
		if (!disabled) {
            cancelDisconnect();
			cancelHeartbeat();
			scheduleHeartbeat();
            scheduleDisconnect();
		}
	}
	
	private void cancelHeartbeat() {
		if (hTimeout != null && !hTimeout.isCancelled()) {
            hTimeout.cancel();
		}
	}

    private void cancelDisconnect(){
        if(dTimeout != null && dTimeout.isCancelled()) {
            dTimeout.cancel();
        }
    }
	
	public void disableHeartbeat() {
		disabled = true;
	}
	
	private void scheduleHeartbeat() {
        hTimeout = hashedWheelTimer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (!disabled) {
                    session.sendHeartbeat();
                    scheduleHeartbeat();
                }
            }
        },heartbeatInterval, TimeUnit.SECONDS);

	}
	
	public void scheduleDisconnect() {
		dTimeout = hashedWheelTimer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (!disabled) {
                    log.debug("{} Session will be disconnected due missed Heartbeat", session.getSessionId());
                    session.disconnect();
                }
            }
        },heartbeatTimeout, TimeUnit.SECONDS);


	}
}
