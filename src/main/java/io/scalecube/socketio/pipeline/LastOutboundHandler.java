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
