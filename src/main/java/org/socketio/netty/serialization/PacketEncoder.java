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
import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.CharsetUtil;
import org.socketio.netty.packets.Packet;

/**
 * Class that provides encoding Socket.IO packets according to specification
 * below.
 * 
 * <h1>Encoding</h1>
 * <p/>
 * Messages have to be encoded before they're sent. The structure of a message
 * is as follows:
 * <p/>
 * {@code [message type] ':' [message id ('+')] ':' [message endpoint] (':' [message
 * data])}
 * <p/>
 * The message type is a single digit integer.
 * <p/>
 * The message id is an incremental integer, required for ACKs (can be
 * ommitted). If the message id is followed by a {@code +}, the ACK is not handled by
 * socket.io, but by the user instead.
 * <p/>
 * Socket.IO has built-in support for multiple channels of communication (which
 * we call "multiple sockets"). Each socket is identified by an endpoint (can be
 * omitted).
 * 
 * @author Anton Kharenko
 * 
 */
public final class PacketEncoder {

	private static final String DELIMITER = ":";
	private static final byte[] DELIMITER_BYTES = DELIMITER.getBytes(CharsetUtil.UTF_8);
	private static final int DELIMITER_LENGTH = DELIMITER_BYTES.length;

	/**
	 * Don't let anyone instantiate this class.
	 */
	private PacketEncoder() {
	}

	public static ByteBuf encodePacket(final Packet packet) throws IOException {
		ByteBuf dataBytes = packet.getData();
		boolean hasData = dataBytes != null;

		CompositeByteBuf compositeByteBuf = PooledByteBufAllocator.DEFAULT.compositeBuffer(hasData ? 1 : 2);

		byte[] typeBytes = packet.getType().getValueAsBytes();
		int headerCapacity = typeBytes.length + DELIMITER_LENGTH + DELIMITER_LENGTH + (hasData ? DELIMITER_LENGTH : 0);
		ByteBuf headerByteBuf = PooledByteBufAllocator.DEFAULT.buffer(headerCapacity, headerCapacity);
		headerByteBuf.writeBytes(typeBytes);
		headerByteBuf.writeBytes(DELIMITER_BYTES);
		headerByteBuf.writeBytes(DELIMITER_BYTES);
		if (hasData) {
			headerByteBuf.writeBytes(DELIMITER_BYTES);
		}
		compositeByteBuf.addComponent(headerByteBuf);
		int compositeReadableBytes = headerByteBuf.readableBytes();

		if (hasData) {
			compositeByteBuf.addComponent(dataBytes);
			compositeReadableBytes += dataBytes.readableBytes();
		}

		compositeByteBuf.writerIndex(compositeReadableBytes);
		return compositeByteBuf;
	}
}
