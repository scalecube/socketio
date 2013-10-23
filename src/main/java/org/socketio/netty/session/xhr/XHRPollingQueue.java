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
package org.socketio.netty.session.xhr;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.socketio.netty.TransportType;
import org.socketio.netty.packets.IPacket;
import org.socketio.netty.packets.PacketsFrame;


public class XHRPollingQueue {

	private final ConcurrentLinkedQueue<IPacket> messagesQueue = new ConcurrentLinkedQueue<IPacket>();
	
	public XHRPollingQueue() {
	}

	public PacketsFrame takeAll() {
		PacketsFrame frame = new PacketsFrame();
		frame.setTransportType(TransportType.XHR_POLLING);
		IPacket item = null;
		while((item = messagesQueue.poll()) != null) {
			frame.getPackets().add(item);
		}
		return frame;
	}
		
	public void add(final IPacket packet) {
		if (packet != null) {
			messagesQueue.add(packet);
		}
	}
	
	public boolean isEmpty() {
		return messagesQueue.isEmpty();
	}

}
