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
package org.socketio.netty.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socketio.netty.ISession;
import org.socketio.netty.ISocketIOListener;

public class EchoSocketIOListener implements ISocketIOListener {
	
	private static final Logger log = LoggerFactory.getLogger(EchoSocketIOListener.class);
	
	@Override
	public void onConnect(final ISession client) {
		log.info("{}/{}: onConnect", client.getSessionId(), client.getLocalPort());
	}

	@Override
	public void onJsonObject(final ISession client, final Object obj) {
		log.info("{}/{}: onJsonObject: {}", new Object [] {client.getSessionId(), client.getLocalPort(), obj});
		processReceivedMessage(client, obj.toString());
	}

	@Override
	public void onMessage(final ISession client, final String message) {
		log.info("{}/{}: onMessage: {}", 
				new Object [] {client.getSessionId(), client.getLocalPort(), message});
		processReceivedMessage(client, message);
	}
	
	private void processReceivedMessage(final ISession client, final String message) {
        client.send(message);  
	}

	@Override
	public void onDisconnect(final ISession client) {
		log.info("{}/{}: onDisconnect", client.getSessionId(), client.getLocalPort());
	}

}
