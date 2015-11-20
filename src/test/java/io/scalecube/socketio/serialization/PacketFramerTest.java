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
package io.scalecube.socketio.serialization;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.scalecube.socketio.packets.Packet;
import io.scalecube.socketio.packets.PacketType;
import io.scalecube.socketio.packets.PacketsFrame;

/**
 *
 * @author Anton Kharenko
 *
 */
public class PacketFramerTest {

  @Test
  public void testDecodingMessagesFrame() throws IOException {
    // Given
    String messagesFrame =
        "\ufffd3\ufffd0::" +
            "\ufffd3\ufffd0::" +
            "\ufffd7\ufffd3:::53d" +
            "\ufffd3\ufffd2::";

    // When
    List<Packet> packets = PacketFramer.decodePacketsFrame(Unpooled.copiedBuffer(messagesFrame.getBytes(CharsetUtil.UTF_8)));

    // Then
    Assert.assertEquals(4, packets.size());
    Assert.assertEquals(PacketType.DISCONNECT, packets.get(0).getType());
    Assert.assertEquals(PacketType.DISCONNECT, packets.get(1).getType());
    Assert.assertEquals(PacketType.MESSAGE, packets.get(2).getType());
    Assert.assertEquals("53d", packets.get(2).getData().toString(CharsetUtil.UTF_8));
    Assert.assertEquals(PacketType.HEARTBEAT, packets.get(3).getType());
  }

  @Test
  public void testDecodingMessagesFrameWithUtf8Symbols() throws IOException {
    // Given
    String messagesFrame =
        "\ufffd3\ufffd0::" +
            "\ufffd36\ufffd3:::{\"ID\":100, \"greetings\":\"Привет\"}" +
            "\ufffd7\ufffd3:::53d";

    // When
    List<Packet> packets = PacketFramer.decodePacketsFrame(Unpooled.copiedBuffer(messagesFrame.getBytes(CharsetUtil.UTF_8)));

    // Then
    Assert.assertEquals(3, packets.size());
    Assert.assertEquals(PacketType.DISCONNECT, packets.get(0).getType());
    Assert.assertEquals(PacketType.MESSAGE, packets.get(1).getType());
    Assert.assertEquals("{\"ID\":100, \"greetings\":\"Привет\"}", packets.get(1).getData().toString(CharsetUtil.UTF_8));
    Assert.assertEquals(PacketType.MESSAGE, packets.get(2).getType());
    Assert.assertEquals("53d", packets.get(2).getData().toString(CharsetUtil.UTF_8));
  }

  @Test
  public void testEncodePacketsFrame() throws IOException {
    // Given
    Packet packet1 = new Packet(PacketType.MESSAGE);
    packet1.setData(Unpooled.copiedBuffer("5", CharsetUtil.UTF_8));
    Packet packet2 = new Packet(PacketType.MESSAGE);
    packet2.setData(Unpooled.copiedBuffer("53d", CharsetUtil.UTF_8));
    PacketsFrame packetsFrame = new PacketsFrame();
    packetsFrame.getPackets().add(packet1);
    packetsFrame.getPackets().add(packet2);

    // When
    String result = PacketFramer.encodePacketsFrame(packetsFrame).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("\ufffd5\ufffd3:::5\ufffd7\ufffd3:::53d", result);
  }

  @Test
  public void testEncodePacketsFrameWithOnePacket() throws IOException {
    // Given
    Packet packet1 = new Packet(PacketType.MESSAGE);
    packet1.setData(Unpooled.copiedBuffer("5", CharsetUtil.UTF_8));
    PacketsFrame packetsFrame = new PacketsFrame();
    packetsFrame.getPackets().add(packet1);

    // When
    String result = PacketFramer.encodePacketsFrame(packetsFrame).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("3:::5", result);
  }

  @Test
  public void testEncodePacketsFrameWithUtf8Symbols() throws IOException {
    // Given
    Packet packet1 = new Packet(PacketType.MESSAGE);
    packet1.setData(Unpooled.copiedBuffer("5", CharsetUtil.UTF_8));
    Packet packet2 = new Packet(PacketType.MESSAGE);
    packet2.setData(Unpooled.copiedBuffer("{\"ID\":100, \"greetings\":\"Привет\"}", CharsetUtil.UTF_8));
    Packet packet3 = new Packet(PacketType.MESSAGE);
    packet3.setData(Unpooled.copiedBuffer("53d", CharsetUtil.UTF_8));
    PacketsFrame packetsFrame = new PacketsFrame();
    packetsFrame.getPackets().add(packet1);
    packetsFrame.getPackets().add(packet2);
    packetsFrame.getPackets().add(packet3);

    // When
    String result = PacketFramer.encodePacketsFrame(packetsFrame).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("\ufffd5\ufffd3:::5\ufffd36\ufffd3:::{\"ID\":100, \"greetings\":\"Привет\"}\ufffd7\ufffd3:::53d", result);
  }

}
