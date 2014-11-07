package org.socketio.netty.session;

import io.netty.util.HashedWheelTimer;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jmock.Mockery;

import java.util.concurrent.TimeUnit;


public class SocketIOHeartbeatSchedulerTest {

    private HashedWheelTimer timer;
    private SocketIOHeartbeatScheduler scheduler;

    private Mockery jmockContext;
    private IManagedSession session;

    @Before
    public void init(){
        timer = new HashedWheelTimer();
        timer.start();
        jmockContext = new JUnit4Mockery() {{
            setThreadingPolicy(new Synchroniser());
        }};
        SocketIOHeartbeatScheduler.setHashedWheelTimer(timer);
        SocketIOHeartbeatScheduler.setHeartbeatInterval(1);
        SocketIOHeartbeatScheduler.setHeartbeatTimeout(3);
        session = jmockContext.mock(IManagedSession.class);
        scheduler = new SocketIOHeartbeatScheduler(session);
    }

    @After
    public void destroy(){
        timer.stop();
    }

    @Test
    public void testReschedule() throws Exception {

        jmockContext.checking(new Expectations() {
            {
                exactly(2).of(session).sendHeartbeat();
                oneOf(session).disconnect();
                oneOf(session).getSessionId();
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