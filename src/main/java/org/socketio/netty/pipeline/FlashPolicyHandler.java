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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class FlashPolicyHandler extends SimpleChannelUpstreamHandler {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private final ChannelBuffer policyRequestBuffer = ChannelBuffers.copiedBuffer("<policy-file-request/>", CharsetUtil.UTF_8);

	private final ChannelBuffer policyResponseBuffer = ChannelBuffers.copiedBuffer(
							"<?xml version=\"1.0\"?>"
							+ "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">"
							+ "<cross-domain-policy> "
							+ "   <site-control permitted-cross-domain-policies=\"master-only\"/>"
							+ "   <allow-access-from domain=\"*\" to-ports=\"*\" />"
							+ "</cross-domain-policy>", CharsetUtil.UTF_8);

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof ChannelBuffer) {
			ChannelBuffer message = (ChannelBuffer) msg;
			if (message.readableBytes() >= policyRequestBuffer.readableBytes()) {
				ChannelBuffer data = message.slice(0, policyRequestBuffer.readableBytes());
	            if (data.equals(policyRequestBuffer)) {
	            	// Remove SSL handler from pipeline otherwise on channel close SSL handler 
	            	// will fail all pending writes instead of flushing them and as a result 
	            	// client won't get flash policy file.
	            	if (ctx.getPipeline().get(SocketIOPipelineFactory.SSL_HANDLER) != null) {
	            		ctx.getPipeline().remove(SocketIOPipelineFactory.SSL_HANDLER);
	            	}
	            	
	            	// Send flash policy file and close connection
	                ChannelFuture f = ctx.getChannel().write(ChannelBuffers.copiedBuffer(policyResponseBuffer));
	                f.addListener(ChannelFutureListener.CLOSE);
	                log.debug("Sent flash policy file to channel: {}", ctx.getChannel());
	                return;
	            }
			}
			ctx.getPipeline().remove(this);
        }
		super.messageReceived(ctx, e);
	}

}
