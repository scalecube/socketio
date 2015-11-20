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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.scalecube.socketio.packets.Packet;
import io.scalecube.socketio.packets.PacketType;

/**
 *
 * @author Anton Kharenko
 *
 */
public class PacketEncoderTest {

  @Test
  public void testEncodeAckPacket() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.ACK);
    packet.setData(Unpooled.copiedBuffer("140", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("6:::140", result);
  }

  @Test
  public void testEncodeAckPacketWithArgs() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.ACK);
    packet.setData(Unpooled.copiedBuffer("12+[\"woot\",\"wa\"]", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("6:::12+[\"woot\",\"wa\"]", result);
  }

  @Test
  public void testEncodeHeartbeatPacket() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.HEARTBEAT);

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("2::", result);
  }

  @Test
  public void testEncodeDisconnectPacket() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.DISCONNECT);
    //packet.setEndpoint("/woot");

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    //Assert.assertEquals("0::/woot", result);
    Assert.assertEquals("0::", result);
  }

  @Test
  public void testEncodeConnectPacket() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.CONNECT);
    //packet.setEndpoint("/tobi");

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    //Assert.assertEquals("1::/tobi", result);
    Assert.assertEquals("1::", result);
  }

  @Test
  public void testEncodeConnectPacketWithQueryString() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.CONNECT);
    //packet.setEndpoint("/test");
    packet.setData(Unpooled.copiedBuffer("?test=1", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    //Assert.assertEquals("1::/test:?test=1", result);
    Assert.assertEquals("1:::?test=1", result);
  }

  @Test
  public void testEncodeErrorPacket() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.ERROR);

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("7::", result);
  }

  @Test
  public void testEncodeErrorPacketWithReason() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.ERROR);
    packet.setData(Unpooled.copiedBuffer("0", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("7:::0", result);
  }

  @Test
  public void testEncodeErrorPacketWithReasonAndAdvice() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.ERROR);
    packet.setData(Unpooled.copiedBuffer("2+0", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("7:::2+0", result);
  }

  @Test
  public void testEncodeErrorPacketWithEndpoint() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.ERROR);
    //packet.setEndpoint("/woot");

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    //Assert.assertEquals("7::/woot", result);
    Assert.assertEquals("7::", result);
  }

  @Test
  public void testEncodeEventPacket() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.EVENT);
    packet.setData(Unpooled.copiedBuffer("{\"name\":\"woot\"}", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("5:::{\"name\":\"woot\"}", result);
  }

  @Test
  public void testEncodeEventPacketWithMessageIdAndAck() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.EVENT);
    //packet.setId("1+");
    packet.setData(Unpooled.copiedBuffer("{\"name\":\"tobi\"}", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    //Assert.assertEquals("5:1+::{\"name\":\"tobi\"}", result);
    Assert.assertEquals("5:::{\"name\":\"tobi\"}", result);
  }

  @Test
  public void testEncodeEventPacketWithData() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.EVENT);
    packet.setData(Unpooled.copiedBuffer("{\"name\":\"edwald\",\"args\":[{\"a\":\"b\"},2,\"3\"]}", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("5:::{\"name\":\"edwald\",\"args\":[{\"a\":\"b\"},2,\"3\"]}", result);
  }

  @Test
  public void testEncodeJsonPacket() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.JSON);
    packet.setData(Unpooled.copiedBuffer("\"2\"", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("4:::\"2\"", result);
  }

  @Test
  public void testEncodeJsonPacketWithMessageIdAndAckData() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.JSON);
    //packet.setId("1+");
    packet.setData(Unpooled.copiedBuffer("{\"a\":\"b\"}", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    //Assert.assertEquals("4:1+::{\"a\":\"b\"}", result);
    Assert.assertEquals("4:::{\"a\":\"b\"}", result);
  }

  @Test
  public void testEncodeMessagePacket() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.MESSAGE);
    packet.setData(Unpooled.copiedBuffer("woot", CharsetUtil.UTF_8));

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    Assert.assertEquals("3:::woot", result);
  }

  @Test
  public void testEncodeMessagePacketWithIdAndEndpoint() throws IOException {
    // Given
    Packet packet = new Packet(PacketType.MESSAGE);
    //packet.setId("5");
    //packet.setEndpoint("/tobi");

    // When
    String result = PacketEncoder.encodePacket(packet).toString(CharsetUtil.UTF_8);

    // Then
    //Assert.assertEquals("3:5:/tobi", result);
    Assert.assertEquals("3::", result);
  }

}
