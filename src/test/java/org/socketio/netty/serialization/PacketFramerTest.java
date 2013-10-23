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
package org.socketio.netty.serialization;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;
import org.socketio.netty.packets.PacketsFrame;
import org.socketio.netty.serialization.PacketFramer;

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
			"\ufffd9\ufffd0::/woot5" +
			"\ufffd7\ufffd3:::53d" +
			"\ufffd3\ufffd2::";
		byte[] messagesFrameBytes = messagesFrame.getBytes("UTF-8"); 
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(messagesFrameBytes.length);
		buffer.writeBytes(messagesFrameBytes);
		
		// When
		List<Packet> packets = new LinkedList<Packet>();
		while (buffer.readable()) {
			Packet packet = PacketFramer.decodeNextPacket(buffer);
			packets.add(packet);
		}

		// Then
		Assert.assertEquals(4, packets.size());
		Assert.assertEquals(PacketType.DISCONNECT, packets.get(0).getType());
		Assert.assertEquals(PacketType.DISCONNECT, packets.get(1).getType());
		Assert.assertEquals("/woot5", packets.get(1).getEndpoint());
		Assert.assertEquals(PacketType.MESSAGE, packets.get(2).getType());
		Assert.assertEquals("53d", packets.get(2).getData());
		Assert.assertEquals(PacketType.HEARTBEAT, packets.get(3).getType());
	}
	
	@Test
	public void testDecodingMessagesFrameWithUtf8Symbols() throws IOException {
		// Given
		String messagesFrame =
			"\ufffd3\ufffd0::" +
			"\ufffd36\ufffd3:::{\"ID\":100, \"greetings\":\"Привет\"}" +
			"\ufffd7\ufffd3:::53d";
		byte[] messagesFrameBytes = messagesFrame.getBytes("UTF-8"); 
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(messagesFrameBytes.length);
		buffer.writeBytes(messagesFrameBytes);

		// When
		List<Packet> packets = new LinkedList<Packet>();
		while (buffer.readable()) {
			Packet packet = PacketFramer.decodeNextPacket(buffer);
			packets.add(packet);
		}

		// Then
		Assert.assertEquals(3, packets.size());
		Assert.assertEquals(PacketType.DISCONNECT, packets.get(0).getType());
		Assert.assertEquals(PacketType.MESSAGE, packets.get(1).getType());
		Assert.assertEquals("{\"ID\":100, \"greetings\":\"Привет\"}", packets.get(1).getData());
		Assert.assertEquals(PacketType.MESSAGE, packets.get(2).getType());
		Assert.assertEquals("53d", packets.get(2).getData());
	}
	
	@Test
    public void testEncodePacketsFrame() throws IOException {
        // Given
		Packet packet1 = new Packet(PacketType.MESSAGE);
        packet1.setData("5");
        Packet packet2 = new Packet(PacketType.MESSAGE);
        packet2.setData("53d");
        PacketsFrame packetsFrame = new PacketsFrame();
        packetsFrame.getPackets().add(packet1);
        packetsFrame.getPackets().add(packet2);
        
        // When
        CharSequence result = PacketFramer.encodePacketsFrame(packetsFrame);
        
        // Then
        Assert.assertEquals("\ufffd5\ufffd3:::5\ufffd7\ufffd3:::53d", result.toString());
    }
	
	@Test
    public void testEncodePacketsFrameWithOnePacket() throws IOException {
        // Given
		Packet packet1 = new Packet(PacketType.MESSAGE);
        packet1.setData("5");
        PacketsFrame packetsFrame = new PacketsFrame();
        packetsFrame.getPackets().add(packet1);
        
        // When
        CharSequence result = PacketFramer.encodePacketsFrame(packetsFrame);
        
        // Then
        Assert.assertEquals("3:::5", result.toString());
    }
	
	@Test
    public void testEncodePacketsFrameWithUtf8Symbols() throws IOException {
        // Given
		Packet packet1 = new Packet(PacketType.MESSAGE);
        packet1.setData("5");
		Packet packet2 = new Packet(PacketType.MESSAGE);
        packet2.setData("{\"ID\":100, \"greetings\":\"Привет\"}");
        Packet packet3 = new Packet(PacketType.MESSAGE);
        packet3.setData("53d");
        PacketsFrame packetsFrame = new PacketsFrame();
        packetsFrame.getPackets().add(packet1);
        packetsFrame.getPackets().add(packet2);
        packetsFrame.getPackets().add(packet3);
        
        // When
        CharSequence result = PacketFramer.encodePacketsFrame(packetsFrame);
        
        // Then
        Assert.assertEquals("\ufffd5\ufffd3:::5\ufffd36\ufffd3:::{\"ID\":100, \"greetings\":\"Привет\"}\ufffd7\ufffd3:::53d", result.toString());
    }
	
}
