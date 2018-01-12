package io.scalecube.socketio.pipeline;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class HeadRequestHandler extends ChannelInboundHandlerAdapter {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            final HttpRequest req = (HttpRequest) msg;
            final HttpMethod requestMethod = req.method();
            if (HttpMethod.HEAD.equals(requestMethod)) {
                String requestPath = new QueryStringDecoder(req.uri()).path();
                if (log.isDebugEnabled()) {
                    log.debug("Received HTTP HEAD request: {} {} from channel: {}", requestPath, ctx.channel());
                }
                sendEmpty(ctx, req);
                ReferenceCountUtil.release(req);
                return;
            }
        }
        super.channelRead(ctx, msg);
    }

    private void sendEmpty(ChannelHandlerContext ctx, HttpRequest req) {
        // Send empty response with headers set
        HttpResponse res = PipelineUtils.createHttpResponse(PipelineUtils.getOrigin(req), Unpooled.EMPTY_BUFFER, false);
        ChannelFuture f = ctx.writeAndFlush(res);
        f.addListener(ChannelFutureListener.CLOSE);
        if (log.isDebugEnabled())
            log.debug("Sent empty response to channel: {}", ctx.channel());
    }
}
