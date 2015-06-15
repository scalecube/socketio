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
package io.servicefabric.socketio.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;


public class HandshakeHandlerTest {

    private static final int PROTOCOL = 1;
    private static final String CONTEXT_PATH = "/socket.io";
    private static final String HANDSHAKE_PATH = CONTEXT_PATH + "/" + PROTOCOL + "/";
    private HandshakeHandler handshakeHandler;

    @Before
    public void setUp() throws Exception {
        handshakeHandler = new HandshakeHandler(HANDSHAKE_PATH,60,60,"websocket,flashsocket,xhr-polling,jsonp-polling");
    }

    @Test
    public void testChannelRead() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/socket.io/1/");

        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, handshakeHandler);
        channel.writeInbound(request);
        Object outboundMessage = lastOutboundHandler.getOutboundMessages().poll();
        Assert.assertTrue(outboundMessage instanceof FullHttpResponse);
        FullHttpResponse res = (FullHttpResponse) outboundMessage;
        Assert.assertEquals(HttpVersion.HTTP_1_1,res.getProtocolVersion());
        Assert.assertEquals(HttpResponseStatus.OK, res.getStatus());
        ByteBuf content = res.content();
        Assert.assertTrue(content.toString(CharsetUtil.UTF_8).endsWith("60:60:websocket,flashsocket,xhr-polling,jsonp-polling"));
        channel.finish();
    }

    @Test
    public void testChannelReadError() throws Exception{
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,"/wrongPath/");

        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, handshakeHandler);
        channel.writeInbound(request);
        Object outboundMessage = lastOutboundHandler.getOutboundMessages().poll();
        Assert.assertTrue(outboundMessage instanceof HttpResponse);
        HttpResponse res = (HttpResponse) outboundMessage;
        Assert.assertEquals(HttpVersion.HTTP_1_1, res.getProtocolVersion());
        Assert.assertEquals(HttpResponseStatus.BAD_REQUEST, res.getStatus());
        channel.finish();
    }

    @Test
    public void testChannelReadPost() throws Exception{
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST,"/socket.io/1/");
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, handshakeHandler);
        channel.writeInbound(request);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof HttpRequest);
        Assert.assertEquals(request, object);
        channel.finish();
    }

    @Test
    public void testChannelReadNonHttp() throws Exception{
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, handshakeHandler);
        channel.writeInbound(Unpooled.EMPTY_BUFFER);
        Object object = channel.readInbound();
        Assert.assertTrue(object instanceof ByteBuf);
        Assert.assertEquals(Unpooled.EMPTY_BUFFER, object);
        channel.finish();
    }
}
