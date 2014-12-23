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
import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;

import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * 
 * @author Anton Kharenko
 *
 */
public class PacketDecoderTest {
	
	@Test
    public void testDecodeDisconnecPacket() throws IOException {
    	// Given
    	String message = "0::/woot";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.DISCONNECT, packet.getType());
        //Assert.assertEquals("/woot", packet.getEndpoint());
    }
	
	@Test
    public void testDecodeConnectPacketWithEndpoint() throws IOException {
    	// Given
    	String message = "1::/tobi";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.CONNECT, packet.getType());
        //Assert.assertEquals("/tobi", packet.getEndpoint());
    }

    @Test
    public void testDecodeConnectPacketWithQuery() throws IOException {
    	// Given
    	String message = "1::/test:?test=1";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.CONNECT, packet.getType());
        //Assert.assertEquals("/test", packet.getEndpoint());
        Assert.assertEquals("?test=1", packet.getData());
    }
    
    @Test
    public void testDecodeHeartbeatPacket() throws IOException {
    	// Given
    	String message = "2:::";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.HEARTBEAT, packet.getType());
    }
    
    @Test
    public void testDecodeMessagePacket() throws IOException {
    	// Given
    	String message = "3:::woot";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.MESSAGE, packet.getType());
        Assert.assertEquals("woot", packet.getData());
    }

    @Test
    public void testDecodeMessagePacketWithIdAndEndpoint() throws IOException {
    	// Given
    	String message = "3:5:/tobi";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.MESSAGE, packet.getType());
        Assert.assertEquals("5", packet.getId());
        //Assert.assertEquals("/tobi", packet.getEndpoint());
    }
    
    @Test
    public void testDecodeJsonPacket() throws IOException {
    	// Given
    	String message = "4:::\"2\"";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.JSON, packet.getType());
        Assert.assertEquals("\"2\"", packet.getData());
    }

    @Test
    public void testDecodeJsonPacketWithMessageIdAndAckData() throws IOException {
    	// Given
    	String message = "4:1+::{\"a\":\"b\"}";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.JSON, packet.getType());
        Assert.assertEquals("1+", packet.getId());
        Assert.assertEquals("{\"a\":\"b\"}", packet.getData());
    }
    
    @Test
    public void testDecodeJsonPacketWithUTF8Symbols() throws IOException {
    	// Given
    	String message = "4:::\"Привет\"";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.JSON, packet.getType());
        Assert.assertEquals("\"Привет\"", packet.getData());
    }
    
    @Test
    public void testDecodeEventPacket() throws IOException {
    	// Given
    	String message = "5:::{\"name\":\"woot\"}";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.EVENT, packet.getType());
        Assert.assertEquals("{\"name\":\"woot\"}", packet.getData());
    }

    @Test
    public void testDecodeEventPacketWithMessageIdAndAck() throws IOException {
    	// Given
    	String message = "5:1+::{\"name\":\"tobi\"}";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.EVENT, packet.getType());
        Assert.assertEquals("1+", packet.getId());
        Assert.assertEquals("{\"name\":\"tobi\"}", packet.getData());
    }

    @Test
    public void testDecodeEventPacketWithData() throws IOException {
    	// Given
    	String message = "5:::{\"name\":\"edwald\",\"args\":[{\"a\": \"b\"},2,\"3\"]}";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.EVENT, packet.getType());
        Assert.assertEquals("{\"name\":\"edwald\",\"args\":[{\"a\": \"b\"},2,\"3\"]}", packet.getData());
    }
	
	@Test
	public void testDecodeAckPacket() throws IOException {
		// Given
		String message = "6:::140";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
		
		// When
		Packet packet = PacketDecoder.decodePacket(byteMessage);
		
		// Then
        Assert.assertEquals(PacketType.ACK, packet.getType());
        Assert.assertEquals("140", packet.getData());
	}
	
	@Test
    public void testDecodeAckPacketWithArgs() throws IOException {
		// Given
		String message = "6:::12+[\"woot\",\"wa\"]";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
		
		// When
		Packet packet = PacketDecoder.decodePacket(byteMessage);
		
		// Then
        Assert.assertEquals(PacketType.ACK, packet.getType());
        Assert.assertEquals("12+[\"woot\",\"wa\"]", packet.getData());
    }

    @Test
	@Ignore // Removed verification of JSON correctness during packet decoding phase
    public void testDecodeAckPacketWithBadJson() throws IOException {
    	// Given
    	String message = "6:::1+{\"++]";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	try {
	    	// When
	    	PacketDecoder.decodePacket(byteMessage);
	    	
	    	// Then
	    	Assert.fail();
    	} catch (JsonMappingException e) {}
    }
    
    @Test
    public void testDecodeErrorPacket() throws IOException {
    	// Given
    	String message = "7:::";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.ERROR, packet.getType());
    }

    @Test
    public void testDecodeErrorPacketWithReason() throws IOException {
    	// Given
    	String message = "7:::0";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.ERROR, packet.getType());
        Assert.assertEquals("0", packet.getData());
    }

    @Test
    public void testDecodeErrorPacketWithReasonAndAdvice() throws IOException {
    	// Given
    	String message = "7:::2+0";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.ERROR, packet.getType());
        Assert.assertEquals("2+0", packet.getData());
    }

    @Test
    public void testDecodeErrorPacketWithEndpoint() throws IOException {
    	// Given
    	String message = "7::/woot";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.ERROR, packet.getType());
        //Assert.assertEquals("/woot", packet.getEndpoint());
    }
    
    @Test
    public void testDecodeNoopPacket() throws IOException {
    	// Given
    	String message = "8::";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        
        // Then
        Assert.assertEquals(PacketType.NOOP, packet.getType());
    }
    
    @Test
    public void testDecodeNewline() throws IOException {
		String message = "3:::\n";
		ByteBuf byteMessage = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
        Packet packet = PacketDecoder.decodePacket(byteMessage);
        Assert.assertEquals(PacketType.MESSAGE, packet.getType());
        Assert.assertEquals("\n", packet.getData());
    }
    
}
