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
package io.scalecube.socketio.packets;

import io.scalecube.socketio.TransportType;


public abstract class AbstractPacket implements IPacket {

	private final PacketType type;

	private String origin;
	
	private String sessionId;

	private TransportType transportType;
	
	private String jsonpIndexParam;
	
	public AbstractPacket() {
		this(null);
	}

	public AbstractPacket(final PacketType type) {
		this(type, null);
	}
	
	public AbstractPacket(final PacketType type, final String sessionId) {
		this.type = type;
		this.sessionId = sessionId;
	}

	public PacketType getType() {
		return type;
	}

	public void setOrigin(final String origin) {
		this.origin = origin;
	}
	
	public String getOrigin(){
		return this.origin;
	}

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getSessionId(){
		return sessionId;
	}
	
	public void setTransportType(final TransportType transportType) {
		this.transportType = transportType;
	}

	public TransportType getTransportType() {
		return transportType;
	}
	
	public String getJsonpIndexParam() {
		return jsonpIndexParam;
	}

	public void setJsonpIndexParam(String jsonpIndexParam) {
		this.jsonpIndexParam = jsonpIndexParam;
	}
	
	@Override
	public String toString() {
		return "type=" + type + ", origin=" + origin + ", sessionId=" + sessionId + ", transportType=" + transportType + ",jsonpIndexParam="
				+ jsonpIndexParam;
	}

}
