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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.socketio.netty.packets.ErrorAdvice;
import org.socketio.netty.packets.ErrorReason;
import org.socketio.netty.packets.Event;
import org.socketio.netty.packets.Packet;
import org.socketio.netty.packets.PacketType;


public final class PacketDecoder {

	private static final Pattern packetPattern = Pattern.compile("([^:]+):([0-9]+)?(\\+)?:([^:]+)?:?([\\s\\S]*)?");
	private static final Pattern ackPattern = Pattern.compile("^([0-9]+)(\\+)?(.*)");
    
    /**
     * Don't let anyone instantiate this class.
     */
	private PacketDecoder() {}
    
    public static Packet decodePacket(final String msg) throws IOException {
        Matcher matcher = packetPattern.matcher(msg);
        if (!matcher.matches()) {
            return Packet.NULL_INSTANCE;
        }
        String id = extract(matcher, 2);
        String data = extract(matcher, 5);
        int typeId = Integer.valueOf(matcher.group(1));
        PacketType type = PacketType.valueOf(typeId);
        String endpoint = extract(matcher, 4);

        Packet packet = new Packet(type);
        packet.setEndpoint(endpoint);
        packet.setId(id);
        if (id != null) {
            String ackData = extract(matcher, 3);
            if (ackData != null) {
                packet.setAck("data");
            } else {
                packet.setAck(true);
            }
        }

        switch (type) {
        case ERROR:
            String[] pieces = data.split("\\+");
            if (pieces.length > 0 && pieces[0].trim().length() > 0) {
                ErrorReason reason = ErrorReason.valueOf(Integer.valueOf(pieces[0]));
                packet.setReason(reason);
                if (pieces.length > 1) {
                    ErrorAdvice advice = ErrorAdvice.valueOf(Integer.valueOf(pieces[1]));
                    packet.setAdvice(advice);
                }
            }
            break;

        case MESSAGE:
            if (data != null) {
                packet.setData(data);
            } else {
                packet.setData("");
            }
            break;

        case EVENT:
            Event event = JsonObjectMapperProvider.getObjectMapper().readValue(data, Event.class);
            packet.setName(event.getName());
            if (event.getArgs() != null) {
                packet.setArgs(event.getArgs());
            }
            break;

        case JSON:
            Object obj = JsonObjectMapperProvider.getObjectMapper().readValue(data, Object.class);
            packet.setData(obj);
            break;

        case CONNECT:
            packet.setQs(data);
            break;

        case ACK:
            if (data == null) {
                break;
            }
            Matcher ackMatcher = ackPattern.matcher(data);
            if (ackMatcher.matches()) {
                packet.setAckId(ackMatcher.group(1));
                String ackArgsJSON = extract(ackMatcher, 3);
                if (ackArgsJSON != null && ackArgsJSON.trim().length() > 0) {
                    List<?> args = JsonObjectMapperProvider.getObjectMapper().readValue(ackArgsJSON, List.class);
                    packet.setArgs(args);
                }
            }
            break;

        case DISCONNECT:
        case HEARTBEAT:
        case NOOP:
            break;
        }

        return packet;
    }

    private static String extract(final Matcher matcher, final int index) {
        if (index > matcher.groupCount()) {
            return null;
        }
        return matcher.group(index);
    }

}
