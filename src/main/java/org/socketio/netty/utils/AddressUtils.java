/**
 * 
 */
package org.socketio.netty.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * AddressUtils.
 * @author <a href="mailto:Andrey.Nechet@playtech.com">Andrey Nechet</a>
 *
 */
public class AddressUtils {

	/**
	 * @param remoteAddress the remoteAddress to set
	 * @throws Exception 
	 */
	public static SocketAddress toSocketAddress(String remoteAddress) throws Exception {
		if (remoteAddress == null || remoteAddress.trim().isEmpty()) {
			return null;
		}
		return new InetSocketAddress(InetAddress.getByName(remoteAddress), 0);
	}

}
