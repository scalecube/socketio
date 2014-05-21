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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.socketio.netty.TransportType;
import org.socketio.netty.packets.ConnectPacket;
import org.socketio.netty.packets.IPacket;
import org.socketio.netty.packets.Packet;


public class WebSocketHandlerTest {

    private static final int PROTOCOL = 1;
    private static final String CONTEXT_PATH = "/socket.io";
    private static final String HANDSHAKE_PATH = CONTEXT_PATH + "/" + PROTOCOL + "/";
    private WebSocketHandler webSocketHandler;

    @Before
    public void setUp() throws Exception {
        webSocketHandler = new WebSocketHandler(HANDSHAKE_PATH,false);
    }

    @Test
    public void testChannelReadNonHttp() throws Exception{
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, webSocketHandler);
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
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, webSocketHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof HttpRequest);
        Assert.assertEquals(request, object);
        channel.finish();
    }

    @Test
    public void testChannelRead() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/socket.io/1/websocket/595f8d1b-a8bb-4453-88ef-b3620dba16c5");
        HttpHeaders.addHeader(request,HttpHeaders.Names.SEC_WEBSOCKET_VERSION, 13);
        HttpHeaders.addHeader(request,"Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits, x-webkit-deflate-frame");
        String origin = "http://localhost:8080";
        HttpHeaders.addHeader(request, HttpHeaders.Names.ORIGIN, origin);
        HttpHeaders.addHeader(request, HttpHeaders.Names.SEC_WEBSOCKET_KEY,"key");
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, webSocketHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof ConnectPacket);
        IPacket packet = (ConnectPacket) object;
        Assert.assertEquals(TransportType.WEBSOCKET ,packet.getTransportType());
        Assert.assertEquals(origin,packet.getOrigin());
        WebSocketFrame webSocketFrame = new TextWebSocketFrame("3:::{\"greetings\":\"Hello World!\"}");
        channel.writeInbound(webSocketFrame);
        object = channel.readInbound();
        Assert.assertTrue(object instanceof Packet);
        packet = (Packet) object;
        Assert.assertEquals(TransportType.WEBSOCKET, packet.getTransportType());
        Assert.assertEquals("{\"greetings\":\"Hello World!\"}", ((Packet)packet).getData());
        channel.finish();
    }

}
