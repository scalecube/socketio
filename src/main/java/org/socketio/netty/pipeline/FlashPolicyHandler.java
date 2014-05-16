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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;

@ChannelHandler.Sharable
public class FlashPolicyHandler extends MessageToMessageDecoder<ByteBuf> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ByteBuf policyRequestBuffer = Unpooled.copiedBuffer("<policy-file-request/>", CharsetUtil.UTF_8);

	private final ByteBuf policyResponseBuffer = Unpooled.copiedBuffer("<?xml version=\"1.0\"?>"
			+ "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">" + "<cross-domain-policy> "
			+ "   <site-control permitted-cross-domain-policies=\"master-only\"/>" + "   <allow-access-from domain=\"*\" to-ports=\"*\" />"
			+ "</cross-domain-policy>", CharsetUtil.UTF_8);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf message, List<Object> out) throws Exception {
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
				ByteBuf buf = ctx.alloc().buffer();
				buf.writeBytes(policyResponseBuffer);
				ChannelFuture f = ctx.writeAndFlush(buf);
				f.addListener(ChannelFutureListener.CLOSE);
				log.debug("Sent flash policy file to channel: {}", ctx.channel());
				return;
			}
		}
		ctx.pipeline().remove(this);
		message.retain();
		out.add(message);
	}
}
