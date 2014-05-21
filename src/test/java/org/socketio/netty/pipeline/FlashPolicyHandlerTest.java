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
import io.netty.util.CharsetUtil;
import org.junit.Before;
import org.junit.Test;

import static io.netty.util.ReferenceCountUtil.releaseLater;
import static org.junit.Assert.*;


public class FlashPolicyHandlerTest {

    private FlashPolicyHandler flashPolicyHandler;

    private final String policyResponse = "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE cross-domain-policy SYSTEM \"/xml/dtds/cross-domain-policy.dtd\">" + "<cross-domain-policy> "
            + "   <site-control permitted-cross-domain-policies=\"master-only\"/>" + "   <allow-access-from domain=\"*\" to-ports=\"*\" />"
            + "</cross-domain-policy>";

    private final ByteBuf policyRequestBuffer = Unpooled.copiedBuffer("<policy-file-request/>", CharsetUtil.UTF_8);




    @Before
    public void init() throws Exception{
        flashPolicyHandler = new FlashPolicyHandler();
    }

    @Test
    public void testDecodeFlashPolicy() throws Exception {
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler,flashPolicyHandler);
        channel.writeInbound(policyRequestBuffer);
        ByteBuf byteBuf = (ByteBuf) lastOutboundHandler.getOutboundMessages().poll();
        assertEquals(policyResponse, releaseLater(byteBuf).toString(CharsetUtil.UTF_8));
        assertTrue(lastOutboundHandler.getOutboundMessages().isEmpty());
        channel.finish();
    }

    @Test
    public void testDecodeNonFlashPolicy() throws Exception {
        LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
        EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler,flashPolicyHandler);
        String message = "{ \"friends\": [" +
                "            {" +
                "                \"id\": 0," +
                "                \"name\": \"Vargas Cochran\"" +
                "            }," +
                "            {" +
                "                \"id\": 1," +
                "                \"name\": \"Gould Marsh\"" +
                "            }," +
                "            {" +
                "                \"id\": 2," +
                "                \"name\": \"Vaughn Contreras\"" +
                "            }" +
                "        ]," +
                "        \"greeting\": \"Hello, Kinney Warren! You have 5 unread messages.\"," +
                "        \"favoriteFruit\": \"banana\"}";
        channel.writeInbound(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
        ByteBuf byteBuf = (ByteBuf) channel.readInbound();
        assertEquals(message, releaseLater(byteBuf).toString(CharsetUtil.UTF_8));
        channel.finish();
    }

}
