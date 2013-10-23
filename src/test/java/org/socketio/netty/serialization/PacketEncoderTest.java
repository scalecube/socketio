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
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.socketio.netty.packets.ErrorAdvice;
import org.socketio.netty.packets.ErrorReason;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;
import org.socketio.netty.serialization.PacketEncoder;

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
        packet.setAckId("140");
    	
    	// When
        String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("6:::140", result);
    }

    @Test
    public void testEncodeAckPacketWithArgs() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.ACK);
        packet.setAckId("12");
        packet.setArgs(Arrays.asList("woot", "wa"));
    	
    	// When
        String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("6:::12+[\"woot\",\"wa\"]", result);
    }
    
    @Test
    public void testEncodeHeartbeatPacket() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.HEARTBEAT);
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("2::", result);
    }

    @Test
    public void testEncodeDisconnectPacket() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.DISCONNECT);
    	packet.setEndpoint("/woot");
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("0::/woot", result);
    }

    @Test
    public void testEncodeConnectPacket() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.CONNECT);
        packet.setEndpoint("/tobi");
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("1::/tobi", result);
    }

    @Test
    public void testEncodeConnectPacketWithQueryString() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.CONNECT);
        packet.setEndpoint("/test");
        packet.setQs("?test=1");
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("1::/test:?test=1", result);
    }
    
    @Test
    public void testEncodeErrorPacket() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.ERROR);
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("7::", result);
    }

    @Test
    public void testEncodeErrorPacketWithReason() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.ERROR);
        packet.setReason(ErrorReason.TRANSPORT_NOT_SUPPORTED);
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("7:::0", result);
    }

    @Test
    public void testEncodeErrorPacketWithReasonAndAdvice() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.ERROR);
        packet.setReason(ErrorReason.UNAUTHORIZED);
        packet.setAdvice(ErrorAdvice.RECONNECT);
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("7:::2+0", result);
    }

    @Test
    public void testEncodeErrorPacketWithEndpoint() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.ERROR);
        packet.setEndpoint("/woot");
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("7::/woot", result);
    }
    
    @Test
    public void testEncodeEventPacket() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.EVENT);
        packet.setName("woot");
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("5:::{\"name\":\"woot\"}", result);
    }

    @Test
    public void testEncodeEventPacketWithMessageIdAndAck() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.EVENT);
        packet.setId("1");
        packet.setAck("data");
        packet.setName("tobi");
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("5:1+::{\"name\":\"tobi\"}", result);
    }

    @Test
    public void testEncodeEventPacketWithData() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.EVENT);
        packet.setName("edwald");
        packet.setArgs(Arrays.asList(Collections.singletonMap("a", "b"), 2, "3"));
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("5:::{\"name\":\"edwald\",\"args\":[{\"a\":\"b\"},2,\"3\"]}", result);
    }
    
    @Test
    public void testEncodeJsonPacket() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.JSON);
        packet.setData("2");
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("4:::\"2\"", result);
    }

    @Test
    public void testEncodeJsonPacketWithMessageIdAndAckData() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.JSON);
        packet.setId("1");
        packet.setAck("data");
        packet.setData(Collections.singletonMap("a", "b"));
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("4:1+::{\"a\":\"b\"}", result);
    }
    
    @Test
    public void testEncodeMessagePacket() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.MESSAGE);
        packet.setData("woot");
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("3:::woot", result);
    }

    @Test
    public void testEncodeMessagePacketWithIdAndEndpoint() throws IOException {
    	// Given
    	Packet packet = new Packet(PacketType.MESSAGE);
        packet.setId("5");
        packet.setAck(true);
        packet.setEndpoint("/tobi");
    	
    	// When
    	String result = PacketEncoder.encodePacket(packet);
        
        // Then
        Assert.assertEquals("3:5:/tobi", result);
    }
	
}
