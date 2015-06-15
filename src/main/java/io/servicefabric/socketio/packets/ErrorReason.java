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
package io.servicefabric.socketio.packets;

public enum ErrorReason {

	TRANSPORT_NOT_SUPPORTED(0),
	SESSION_NOT_HANDSHAKEN(1),
	UNAUTHORIZED(2);

	private final int value;

	ErrorReason(final int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ErrorReason valueOf(final int value) {
		return values()[value];
	}

}
