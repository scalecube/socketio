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
import org.socketio.netty.packets.Packet;

public class JsonpPollingHandlerTest {
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	private static final int PROTOCOL = 1;
    private static final String CONTEXT_PATH = "/socket.io";
    private static final String HANDSHAKE_PATH = CONTEXT_PATH + "/" + PROTOCOL + "/";
    private JsonpPollingHandler jsonpPollingHandler;

    @Before
    public void setUp() throws Exception {
        jsonpPollingHandler = new JsonpPollingHandler(HANDSHAKE_PATH, X_FORWARDED_FOR);
    }
    @Test
    public void testChannelReadNonHttp() throws Exception{
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, jsonpPollingHandler);
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
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, jsonpPollingHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof HttpRequest);
        Assert.assertEquals(request, object);
        channel.finish();
    }

    @Test
    public void testChannelReadConnect() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/socket.io/1/jsonp-polling");
        String origin = "http://localhost:8080";
        HttpHeaders.addHeader(request, HttpHeaders.Names.ORIGIN, origin);
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, jsonpPollingHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof ConnectPacket);
        ConnectPacket packet = (ConnectPacket) object;
        Assert.assertEquals(TransportType.JSONP_POLLING ,packet.getTransportType());
        Assert.assertEquals(origin,packet.getOrigin());
        Assert.assertNull(packet.getRemoteAddress());
        channel.finish();
    }
    @Test
    public void testChannelReadConnectWithClientIpInHeader() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/socket.io/1/jsonp-polling");
        String origin = "http://localhost:8080";
        HttpHeaders.addHeader(request, HttpHeaders.Names.ORIGIN, origin);
        HttpHeaders.addHeader(request, X_FORWARDED_FOR, "1.2.3.4");
        
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, jsonpPollingHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof ConnectPacket);
        ConnectPacket packet = (ConnectPacket) object;
        Assert.assertEquals(TransportType.JSONP_POLLING ,packet.getTransportType());
        Assert.assertEquals(origin,packet.getOrigin());
        Assert.assertEquals("/1.2.3.4:0", packet.getRemoteAddress().toString());
        channel.finish();
    }
    @Test
    public void testChannelReadPacket() throws Exception {
        ByteBuf content = Unpooled.copiedBuffer("d=3:::{\"greetings\":\"Hello World!\"}", CharsetUtil.UTF_8);
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,"/socket.io/1/jsonp-polling", content);
        String origin = "http://localhost:8080";
        HttpHeaders.addHeader(request,HttpHeaders.Names.ORIGIN, origin);
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, jsonpPollingHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof Packet);
        Packet packet = (Packet) object;
        Assert.assertEquals(origin,packet.getOrigin());
        Assert.assertEquals("{\"greetings\":\"Hello World!\"}",packet.getData());
        channel.finish();
    }
}
