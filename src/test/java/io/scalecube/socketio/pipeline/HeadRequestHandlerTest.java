package io.scalecube.socketio.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeadRequestHandlerTest {

    HeadRequestHandler handler = new HeadRequestHandler();

    @Test
    public void testChannelRead() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.HEAD, "/socket.io/1/");

        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, handler);
        channel.writeInbound(request);
        Object outboundMessage = lastOutboundHandler.getOutboundMessages().poll();
        assertTrue(outboundMessage instanceof FullHttpResponse);
        FullHttpResponse res = (FullHttpResponse) outboundMessage;
        assertEquals(HttpVersion.HTTP_1_1, res.protocolVersion());
        assertEquals(HttpResponseStatus.OK, res.status());
        assertEquals("keep-alive", res.headers().get(HttpHeaderNames.CONNECTION));
        assertEquals("0", res.headers().get(HttpHeaderNames.CONTENT_LENGTH));
        ByteBuf content = res.content();
        assertEquals(0, content.readableBytes());
        channel.finish();
    }
}
