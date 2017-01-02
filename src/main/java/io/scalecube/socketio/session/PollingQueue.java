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
package io.scalecube.socketio.session;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.scalecube.socketio.packets.Packet;
import io.scalecube.socketio.packets.PacketsFrame;


public class PollingQueue {

  private final ConcurrentLinkedQueue<Packet> packetQueue = new ConcurrentLinkedQueue<>();

  public PacketsFrame takeAll() {
    PacketsFrame frame = new PacketsFrame();
    Packet packet;
    while ((packet = packetQueue.poll()) != null) {
      frame.getPackets().add(packet);
    }
    return frame;
  }

  public void add(final Packet packet) {
    if (packet != null) {
      packetQueue.add(packet);
    }
  }

  public boolean isEmpty() {
    return packetQueue.isEmpty();
  }

}
