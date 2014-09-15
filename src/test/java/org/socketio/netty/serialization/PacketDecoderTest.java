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
import java.util.Arrays;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.socketio.netty.packets.ErrorAdvice;
import org.socketio.netty.packets.ErrorReason;
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
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.DISCONNECT, packet.getType());
        Assert.assertEquals("/woot", packet.getEndpoint());
    }
	
	@Test
    public void testDecodeConnectPacketWithEndpoint() throws IOException {
    	// Given
    	String message = "1::/tobi";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.CONNECT, packet.getType());
        Assert.assertEquals("/tobi", packet.getEndpoint());
    }

    @Test
    public void testDecodeConnectPacketWithQuery() throws IOException {
    	// Given
    	String message = "1::/test:?test=1";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.CONNECT, packet.getType());
        Assert.assertEquals("/test", packet.getEndpoint());
        Assert.assertEquals("?test=1", packet.getQs());
    }
    
    @Test
    public void testDecodeHeartbeatPacket() throws IOException {
    	// Given
    	String message = "2:::";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.HEARTBEAT, packet.getType());
    }
    
    @Test
    public void testDecodeMessagePacket() throws IOException {
    	// Given
    	String message = "3:::woot";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.MESSAGE, packet.getType());
        Assert.assertEquals("woot", packet.getData());
    }

    @Test
    public void testDecodeMessagePacketWithIdAndEndpoint() throws IOException {
    	// Given
    	String message = "3:5:/tobi";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.MESSAGE, packet.getType());
        Assert.assertEquals("5", packet.getId());
        Assert.assertEquals(true, packet.getAck());
        Assert.assertEquals("/tobi", packet.getEndpoint());
    }
    
    @Test
    public void testDecodeJsonPacket() throws IOException {
    	// Given
    	String message = "4:::\"2\"";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.JSON, packet.getType());
        Assert.assertEquals("2", packet.getData());
    }

    @Test
    public void testDecodeJsonPacketWithMessageIdAndAckData() throws IOException {
    	// Given
    	String message = "4:1+::{\"a\":\"b\"}";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.JSON, packet.getType());
        Assert.assertEquals("1", packet.getId());
        Assert.assertEquals("data", packet.getAck());
        Map<?, ?> obj = (Map<?, ?>) packet.getData();
        Assert.assertEquals("b", obj.get("a"));
        Assert.assertEquals(1, obj.size());
    }
    
    @Test
    public void testDecodeJsonPacketWithUTF8Symbols() throws IOException {
    	// Given
    	String message = "4:::\"Привет\"";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.JSON, packet.getType());
        Assert.assertEquals("Привет", packet.getData());
    }
    
    @Test
    public void testDecodeEventPacket() throws IOException {
    	// Given
    	String message = "5:::{\"name\":\"woot\"}";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.EVENT, packet.getType());
        Assert.assertEquals("woot", packet.getName());
    }

    @Test
    public void testDecodeEventPacketWithMessageIdAndAck() throws IOException {
    	// Given
    	String message = "5:1+::{\"name\":\"tobi\"}";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.EVENT, packet.getType());
        Assert.assertEquals("1", packet.getId());
        Assert.assertEquals("data", packet.getAck());
        Assert.assertEquals("tobi", packet.getName());
    }

    @Test
    public void testDecodeEventPacketWithData() throws IOException {
    	// Given
    	String message = "5:::{\"name\":\"edwald\",\"args\":[{\"a\": \"b\"},2,\"3\"]}";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.EVENT, packet.getType());
        Assert.assertEquals("edwald", packet.getName());
        Assert.assertEquals(3, packet.getArgs().size());
        Map<?, ?> obj = (Map<?, ?>) packet.getArgs().get(0);
        Assert.assertEquals("b", obj.get("a"));
        Assert.assertEquals(2, packet.getArgs().get(1));
        Assert.assertEquals("3", packet.getArgs().get(2));
    }
	
	@Test
	public void testDecodeAckPacket() throws IOException {
		// Given
		String message = "6:::140";
		
		// When
		Packet packet = PacketDecoder.decodePacket(message);
		
		// Then
        Assert.assertEquals(PacketType.ACK, packet.getType());
        Assert.assertEquals("140", packet.getAckId());
        Assert.assertTrue(packet.getArgs().isEmpty());
	}
	
	@Test
    public void testDecodeAckPacketWithArgs() throws IOException {
		// Given
		String message = "6:::12+[\"woot\",\"wa\"]";
		
		// When
		Packet packet = PacketDecoder.decodePacket(message);
		
		// Then
        Assert.assertEquals(PacketType.ACK, packet.getType());
        Assert.assertEquals("12", packet.getAckId());
        Assert.assertEquals(Arrays.asList("woot", "wa"), packet.getArgs());
    }

    @Test
    public void testDecodeAckPacketWithBadJson() throws IOException {
    	// Given
    	String message = "6:::1+{\"++]";
    	
    	try {
	    	// When
	    	PacketDecoder.decodePacket(message);
	    	
	    	// Then
	    	Assert.fail();
    	} catch (JsonMappingException e) {}
    }
    
    @Test
    public void testDecodeErrorPacket() throws IOException {
    	// Given
    	String message = "7:::";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.ERROR, packet.getType());
    }

    @Test
    public void testDecodeErrorPacketWithReason() throws IOException {
    	// Given
    	String message = "7:::0";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.ERROR, packet.getType());
        Assert.assertEquals(ErrorReason.TRANSPORT_NOT_SUPPORTED, packet.getReason());
    }

    @Test
    public void testDecodeErrorPacketWithReasonAndAdvice() throws IOException {
    	// Given
    	String message = "7:::2+0";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.ERROR, packet.getType());
        Assert.assertEquals(ErrorReason.UNAUTHORIZED, packet.getReason());
        Assert.assertEquals(ErrorAdvice.RECONNECT, packet.getAdvice());
    }

    @Test
    public void testDecodeErrorPacketWithEndpoint() throws IOException {
    	// Given
    	String message = "7::/woot";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.ERROR, packet.getType());
        Assert.assertEquals("/woot", packet.getEndpoint());
    }
    
    @Test
    public void testDecodeNoopPacket() throws IOException {
    	// Given
    	String message = "8::";
    	
    	// When
        Packet packet = PacketDecoder.decodePacket(message);
        
        // Then
        Assert.assertEquals(PacketType.NOOP, packet.getType());
    }
    
    @Test
    public void testDecodeNewline() throws IOException {
        Packet packet = PacketDecoder.decodePacket("3:::\n");
        Assert.assertEquals(PacketType.MESSAGE, packet.getType());
        Assert.assertEquals("\n", packet.getData());
    }
    
}
