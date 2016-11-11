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

import io.netty.buffer.ByteBuf;
import io.netty.util.ByteProcessor;
import io.netty.util.CharsetUtil;
import io.scalecube.socketio.packets.Packet;
import io.scalecube.socketio.packets.PacketType;

import java.io.IOException;

public final class PacketDecoder {

  private static final byte DELIMITER = (byte) ':';
  private static final ByteProcessor packetDelimiterFinder = new ByteProcessor() {
    @Override
    public boolean process(byte value) throws Exception {
      return value != DELIMITER;
    }
  };

  /**
   * Don't let anyone instantiate this class.
   */
  private PacketDecoder() {
  }

  public static Packet decodePacket(final ByteBuf payload) throws IOException {
    int payloadSize = payload.readableBytes();

    // Decode packet type
    int typeDelimiterIndex = payload.forEachByte(packetDelimiterFinder);
    if (typeDelimiterIndex == -1) {
      return Packet.NULL_INSTANCE;
    }
    ByteBuf typeBytes = payload.slice(0, typeDelimiterIndex);
    String typeString = typeBytes.toString(CharsetUtil.UTF_8);
    int typeId = Integer.valueOf(typeString);
    PacketType type = PacketType.valueOf(typeId);

    // Skip message id
    int messageIdDelimiterIndex = payload.forEachByte(typeDelimiterIndex + 1, payloadSize - typeDelimiterIndex - 1, packetDelimiterFinder);
    if (messageIdDelimiterIndex == -1) {
      return Packet.NULL_INSTANCE;
    }

    // Skip endpoint
    int endpointDelimiterIndex = payload.forEachByte(messageIdDelimiterIndex + 1, payloadSize - messageIdDelimiterIndex - 1, packetDelimiterFinder);

    // Create instance of packet
    Packet packet = new Packet(type);

    // Decode data
    boolean messagingType = type == PacketType.MESSAGE || type == PacketType.JSON;
    if (endpointDelimiterIndex != -1 && messagingType) {
      int dataLength = payloadSize - endpointDelimiterIndex - 1;
      if (dataLength > 0) {
        ByteBuf data = payload.copy(endpointDelimiterIndex + 1, dataLength);
        packet.setData(data);
      }
    }

    return packet;
  }

}
