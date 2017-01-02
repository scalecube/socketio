/**
 * Copyright 2012 Ronen Hamias, Anton Kharenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.scalecube.socketio.session;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.netty.util.HashedWheelTimer;

public class SocketIOHeartbeatSchedulerTest {

  private HashedWheelTimer timer;
  private SocketIOHeartbeatScheduler scheduler;

  private Mockery jmockContext;
  private ManagedSession session;

  @Before
  public void init() {
    timer = new HashedWheelTimer();
    timer.start();
    jmockContext = new JUnit4Mockery() {{
      setThreadingPolicy(new Synchroniser());
    }};
    SocketIOHeartbeatScheduler.setHashedWheelTimer(timer);
    SocketIOHeartbeatScheduler.setHeartbeatInterval(1);
    SocketIOHeartbeatScheduler.setHeartbeatTimeout(3);
    session = jmockContext.mock(ManagedSession.class);
    scheduler = new SocketIOHeartbeatScheduler(session);
  }

  @After
  public void destroy() {
    timer.stop();
  }

  @Test
  public void testReschedule() throws Exception {

    jmockContext.checking(new Expectations() {
      {
        exactly(2).of(session).sendHeartbeat();
        oneOf(session).disconnect();
      }

    });
    scheduler.reschedule();
    TimeUnit.SECONDS.sleep(4);
    jmockContext.assertIsSatisfied();
  }

  @Test
  public void testRescheduleNotDisconnect() throws Exception {

    jmockContext.checking(new Expectations() {
      {
        exactly(1).of(session).sendHeartbeat();
      }

    });
    scheduler.reschedule();
    TimeUnit.MILLISECONDS.sleep(1500);
    jmockContext.assertIsSatisfied();
  }
}
