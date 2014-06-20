/**
 * 
 */
package org.socketio.netty.utils;

import java.net.Inet4Address;
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
		String[] bytes = remoteAddress.trim().split("\\.");
		byte[] address = new byte[4];
		if (bytes.length == 2) {
			address[0] = Integer.valueOf(bytes[0]).byteValue();
			address[1] = 0;
			address[2] = 0;
			address[3] = Integer.valueOf(bytes[1]).byteValue(); 
		} else if (bytes.length == 4){
			for (int i = 0; i < 4; i++) {
				address[i] = Integer.valueOf(bytes[i]).byteValue(); 
			}
		} else {
			throw new IllegalArgumentException(remoteAddress + " can't be parsed");
		}
		return new InetSocketAddress(Inet4Address.getByAddress(address), 0);
	}

}
