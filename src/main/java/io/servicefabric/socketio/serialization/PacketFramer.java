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
package io.servicefabric.socketio.serialization;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.servicefabric.socketio.packets.Packet;
import io.servicefabric.socketio.packets.PacketsFrame;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * Class which responde for supporting Socket.IO framing as described below.
 * 
 * <h1>Framing</h1>
 * <p/>
 * Certain transports, like websocket or flashsocket, have built-in lightweight
 * framing mechanisms for sending and receiving messages.
 * <p/>
 * For xhr-multipart, the built-in MIME framing is used for the sake of
 * consistency.
 * <p/>
 * When no built-in lightweight framing is available, and multiple messages need
 * to be delivered (i.e: buffered messages), the following is used:
 * <p/>
 * {@code `\ufffd` [message lenth] `\ufffd`}
 * <p/>
 * Transports where the framing overhead is expensive (ie: when the xhr-polling
 * transport tries to send data to the server).
 * 
 * 
 * @author Anton Kharenko
 * 
 */
public final class PacketFramer {

	private static final char DELIMITER = '\ufffd';

	private static final byte[] DELIMITER_BYTES = new String(
			new char[] { DELIMITER }).getBytes(CharsetUtil.UTF_8);

	private static final int DELIMITER_BYTES_SIZE = DELIMITER_BYTES.length;

	/**
	 * Don't let anyone instantiate this class.
	 */
	private PacketFramer() {
	}

	public static ByteBuf encodePacketsFrame(final PacketsFrame packetsFrame) throws IOException {
		List<Packet> packets = packetsFrame.getPackets();
		if (packets.size() == 1) {
			Packet packet = packets.get(0);
			return PacketEncoder.encodePacket(packet);
		} else {
			CompositeByteBuf compositeByteBuf = PooledByteBufAllocator.DEFAULT.compositeBuffer(packets.size() * 2);
			int compositeReadableBytes = 0;

			for (Packet packet : packets) {
				// Decode packet
				ByteBuf packetByteBuf = PacketEncoder.encodePacket(packet);

				// Prepare length prepender
				int packetLength = getUtf8CharCountByByteCount(packetByteBuf, 0, packetByteBuf.readableBytes());
				byte[] packetLengthBytes = String.valueOf(packetLength).getBytes(CharsetUtil.UTF_8);
				int packetLengthPrependerCapacity = packetLengthBytes.length + DELIMITER_BYTES_SIZE + DELIMITER_BYTES_SIZE;
				ByteBuf packetLengthPrependerByteBuf = PooledByteBufAllocator.DEFAULT.buffer(packetLengthPrependerCapacity, packetLengthPrependerCapacity);
				packetLengthPrependerByteBuf.writeBytes(DELIMITER_BYTES);
				packetLengthPrependerByteBuf.writeBytes(packetLengthBytes);
				packetLengthPrependerByteBuf.writeBytes(DELIMITER_BYTES);

				// Update composite byte buffer
				compositeByteBuf.addComponent(packetLengthPrependerByteBuf);
				compositeByteBuf.addComponent(packetByteBuf);
				compositeReadableBytes += packetLengthPrependerByteBuf.readableBytes() + packetByteBuf.readableBytes();
			}

			compositeByteBuf.writerIndex(compositeReadableBytes);
			return compositeByteBuf;
		}
	}

	private static int getUtf8CharCountByByteCount(final ByteBuf buffer, final int startByteIndex, final int bytesCount) {
		int charCount = 0;
		int endByteIndex = startByteIndex + bytesCount;
		int byteIndex = startByteIndex;
		while (byteIndex < endByteIndex) {
			charCount++;
			// Define next char first byte
			short charFirstByte = buffer.getUnsignedByte(byteIndex);

			// Scan first byte of UTF-8 character according to:
			// http://www.cl.cam.ac.uk/~mgk25/unicode.html#utf-8
			if ((charFirstByte >= 0x20) && (charFirstByte <= 0x7F)) {
				// characters U-00000000 - U-0000007F (same as ASCII)
				byteIndex++;
			} else if ((charFirstByte & 0xE0) == 0xC0) {
				// characters U-00000080 - U-000007FF, mask 110XXXXX
				byteIndex += 2;
			} else if ((charFirstByte & 0xF0) == 0xE0) {
				// characters U-00000800 - U-0000FFFF, mask 1110XXXX
				byteIndex += 3;
			} else if ((charFirstByte & 0xF8) == 0xF0) {
				// characters U-00010000 - U-001FFFFF, mask 11110XXX
				byteIndex += 4;
			} else if ((charFirstByte & 0xFC) == 0xF8) {
				// characters U-00200000 - U-03FFFFFF, mask 111110XX
				byteIndex += 5;
			} else if ((charFirstByte & 0xFE) == 0xFC) {
				// characters U-04000000 - U-7FFFFFFF, mask 1111110X
				byteIndex += 6;
			} else {
				byteIndex++;
			}
		}
		return charCount;
	}

	public static List<Packet> decodePacketsFrame(final ByteBuf buffer) throws IOException {
		List<Packet> packets = new LinkedList<Packet>();
		int sequenceNumber = 0;
		while (buffer.isReadable()) {
			Packet packet = PacketFramer.decodeNextPacket(buffer);
			packet.setSequenceNumber(sequenceNumber);
			sequenceNumber++;
			packets.add(packet);
		}
		return packets;
	}

	private static Packet decodeNextPacket(final ByteBuf buffer) throws IOException {
		Packet packet;
		if (isDelimiter(buffer, buffer.readerIndex())) {
			CharSequence packetCharsCountString = decodePacketLength(buffer);
			final Integer packetCharsCount = Integer.valueOf(packetCharsCountString.toString());
			final int packetStartIndex = buffer.readerIndex()
					+ DELIMITER_BYTES_SIZE
					+ packetCharsCountString.length()
					+ DELIMITER_BYTES_SIZE;
			final int packetBytesCount = getUtf8ByteCountByCharCount(buffer, packetStartIndex, packetCharsCount);

			ByteBuf frame = buffer.slice(packetStartIndex, packetBytesCount);

			packet = PacketDecoder.decodePacket(frame);
			buffer.readerIndex(packetStartIndex + packetBytesCount);
		} else {
			packet = PacketDecoder.decodePacket(buffer);
			buffer.readerIndex(buffer.readableBytes());
		}
		return packet;
	}

	private static int getUtf8ByteCountByCharCount(final ByteBuf buffer, final int startIndex, final int charCount) {
		int bytesCount = 0;
		for (int charIndex = 0; charIndex < charCount; charIndex++) {
			// Define next char first byte
			int charFirstByteIndex = startIndex + bytesCount;
			short charFirstByte = buffer.getUnsignedByte(charFirstByteIndex);

			// Scan first byte of UTF-8 character according to:
			// http://www.cl.cam.ac.uk/~mgk25/unicode.html#utf-8
			if ((charFirstByte >= 0x20) && (charFirstByte <= 0x7F)) {
				// characters U-00000000 - U-0000007F (same as ASCII)
				bytesCount++;
			} else if ((charFirstByte & 0xE0) == 0xC0) {
				// characters U-00000080 - U-000007FF, mask 110XXXXX
				bytesCount += 2;
			} else if ((charFirstByte & 0xF0) == 0xE0) {
				// characters U-00000800 - U-0000FFFF, mask 1110XXXX
				bytesCount += 3;
			} else if ((charFirstByte & 0xF8) == 0xF0) {
				// characters U-00010000 - U-001FFFFF, mask 11110XXX
				bytesCount += 4;
			} else if ((charFirstByte & 0xFC) == 0xF8) {
				// characters U-00200000 - U-03FFFFFF, mask 111110XX
				bytesCount += 5;
			} else if ((charFirstByte & 0xFE) == 0xFC) {
				// characters U-04000000 - U-7FFFFFFF, mask 1111110X
				bytesCount += 6;
			} else {
				bytesCount++;
			}
		}
		return bytesCount;
	}

	private static CharSequence decodePacketLength(final ByteBuf buffer) {
		StringBuilder length = new StringBuilder();
		final int scanStartIndex = buffer.readerIndex() + DELIMITER_BYTES_SIZE;
		final int scanEndIndex = buffer.readerIndex() + buffer.readableBytes();
		for (int charIndex = scanStartIndex; charIndex < scanEndIndex; charIndex++) {
			if (isDelimiter(buffer, charIndex)) {
				break;
			} else {
				length.append((char) buffer.getUnsignedByte(charIndex));
			}
		}
		return length;
	}

	private static boolean isDelimiter(final ByteBuf buffer, final int index) {
		for (int i = 0; i < DELIMITER_BYTES_SIZE; i++) {
			if (buffer.getByte(index + i) != DELIMITER_BYTES[i]) {
				return false;
			}
		}
		return true;
	}
}
