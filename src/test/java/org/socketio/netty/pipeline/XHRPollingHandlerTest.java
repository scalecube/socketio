package org.socketio.netty.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.ConnectPacket;
import org.socketio.netty.packets.IPacket;

/**
 * Created by miroslav_l on 5/16/14.
 */
public class XHRPollingHandlerTest {

    private static final int PROTOCOL = 1;
    private static final String CONTEXT_PATH = "/socket.io";
    private static final String HANDSHAKE_PATH = CONTEXT_PATH + "/" + PROTOCOL + "/";
    private XHRPollingHandler xhrPollingHandler;

    @Before
    public void setUp() throws Exception {
        xhrPollingHandler = new XHRPollingHandler(HANDSHAKE_PATH);
    }
    @Test
    public void testChannelReadNonHttp() throws Exception{
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, xhrPollingHandler);
        channel.writeInbound(Unpooled.EMPTY_BUFFER);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof ByteBuf);
        Assert.assertEquals(Unpooled.EMPTY_BUFFER, object);
        channel.finish();
    }


    @Test
    public void testChannelReadWrongPath() throws Exception{
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/wrongPath/");
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, xhrPollingHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof HttpRequest);
        Assert.assertEquals(request, object);
        channel.finish();
    }

    @Test
    public void testChannelReadConnect() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/socket.io/1/xhr-polling");
        String origin = "http://localhost:8080";
        HttpHeaders.addHeader(request,HttpHeaders.Names.ORIGIN, origin);
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, xhrPollingHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof ConnectPacket);
        ConnectPacket packet = (ConnectPacket) object;
        Assert.assertEquals(TransportType.XHR_POLLING ,packet.getTransportType());
        Assert.assertEquals(origin,packet.getOrigin());
        channel.finish();
    }
    @Test
    public void testChannelReadPacket() throws Exception {
        ByteBuf content = Unpooled.copiedBuffer("", CharsetUtil.UTF_8);
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/socket.io/1/xhr-polling");
        String origin = "http://localhost:8080";
        HttpHeaders.addHeader(request,HttpHeaders.Names.ORIGIN, origin);
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, xhrPollingHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof ConnectPacket);
        ConnectPacket packet = (ConnectPacket) object;
        Assert.assertEquals(TransportType.XHR_POLLING ,packet.getTransportType());
        Assert.assertEquals(origin,packet.getOrigin());
        channel.finish();
    }
}
