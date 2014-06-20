/**
 * 
 */
package org.socketio.netty.utils;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HeaderUtils.
 * @author <a href="mailto:Andrey.Nechet@playtech.com">Andrey Nechet</a>
 *
 */
public class HeaderUtils {
	
	private static final Logger log = LoggerFactory.getLogger(HeaderUtils.class);
	
	public static String getHeaderParamValue(HttpMessage message, String paramName) {
		if (paramName == null || paramName.trim().isEmpty()) {
			return null;
		}
		return HttpHeaders.getHeader(message, paramName);
	}

	public static SocketAddress getHeaderClientIPParamValue(HttpMessage message, String paramName) {
		
		SocketAddress result = null;
		
		if (paramName == null || paramName.trim().isEmpty()) {
			;
		} else {
			String ip = null;
			try {
				ip = HttpHeaders.getHeader(message, paramName);
				if (ip != null) {
					result = new InetSocketAddress(InetAddress.getByName(ip), 0);
				}
			} catch (Exception e) {
				log.warn("Failed to parse IP address: {} from http header: {}", ip, paramName);
			}
		}
		return result;
	}
}
