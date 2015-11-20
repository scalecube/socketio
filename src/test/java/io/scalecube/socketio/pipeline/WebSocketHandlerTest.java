/**
 * Copyright 2012 Ronen Hamias, Anton Kharenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.scalecube.socketio.pipeline;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

public class WebSocketHandlerTest {

  private static final int PROTOCOL = 1;
  private static final String CONTEXT_PATH = "/socket.io";
  private static final String HANDSHAKE_PATH = CONTEXT_PATH + "/" + PROTOCOL + "/";
  private WebSocketHandler webSocketHandler;

  @Before
  public void setUp() throws Exception {
    webSocketHandler = new WebSocketHandler(HANDSHAKE_PATH, false, "X-Forwarded-For");
  }

  @Test
  public void testChannelReadNonHttp() throws Exception {
    LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
    EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, webSocketHandler);
    channel.writeInbound(Unpooled.EMPTY_BUFFER);
    Object object = channel.readInbound();
    Assert.assertTrue(object instanceof ByteBuf);
    Assert.assertEquals(Unpooled.EMPTY_BUFFER, object);
    channel.finish();
  }

  @Test
  public void testChannelReadWrongPath() throws Exception {
    HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/wrongPath/");
    LastOutboundHandler lastOutboundHandler = new LastOutboundHandler();
    EmbeddedChannel channel = new EmbeddedChannel(lastOutboundHandler, webSocketHandler);
    channel.writeInbound(request);
    Object object = channel.readInbound();
    Assert.assertTrue(object instanceof HttpRequest);
    Assert.assertEquals(request, object);
    channel.finish();
  }


}
