package org.socketio.netty.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.ArrayDeque;
import java.util.Queue;

/**
* Created by miroslav_l on 5/16/14.
*/
final class LastOutboundHandler extends ChannelOutboundHandlerAdapter {
    private final Queue<Object> outboundMessages = new ArrayDeque<Object>();

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        outboundMessages.add(msg);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        // do nothing
    }

    public Queue<Object> getOutboundMessages() {
        return outboundMessages;
    }
}
