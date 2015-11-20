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
package io.scalecube.socketio.pipeline;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class FlashPolicyHandler extends ChannelInboundHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final static ByteBuf policyRequestBuffer = Unpooled.copiedBuffer("<policy-file-request/>", CharsetUtil.UTF_8);

	private final static String policyResponse ="<?xml version=\"1.0\"?>"
			+ "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">"
            + "<cross-domain-policy> "
			+ "   <site-control permitted-cross-domain-policies=\"master-only\"/>"
            + "   <allow-access-from domain=\"*\" to-ports=\"*\" />"
			+ "</cross-domain-policy>";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof ByteBuf){
            ByteBuf message = (ByteBuf) msg;
            if (message.readableBytes() >= policyRequestBuffer.readableBytes()) {
                ByteBuf data = message.slice(0, policyRequestBuffer.readableBytes());
                if (data.equals(policyRequestBuffer)) {
                    // Remove SSL handler from pipeline otherwise on channel close SSL handler
                    // will fail all pending writes instead of flushing them and as a result
                    // client won't get flash policy file.
                    if (ctx.pipeline().get(SocketIOChannelInitializer.SSL_HANDLER) != null) {
                        ctx.pipeline().remove(SocketIOChannelInitializer.SSL_HANDLER);
                    }

                    // Send flash policy file and close connection
                    ByteBuf response = PipelineUtils.copiedBuffer(ctx.alloc(), policyResponse);
                    ChannelFuture f = ctx.writeAndFlush(response);
                    f.addListener(ChannelFutureListener.CLOSE);
					if (log.isDebugEnabled())
                    	log.debug("Sent flash policy file to channel: {}", ctx.channel());
                    message.release();
                    return;
                }
            }
            ctx.pipeline().remove(this);
        }
        ctx.fireChannelRead(msg);
    }
}
