/**
 * 
 */
package org.socketio.netty.utils;

import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;

/**
 * AddressUtilsTest.
 * @author <a href="mailto:Andrey.Nechet@playtech.com">Andrey Nechet</a>
 *
 */
public class AddressUtilsTest {

	@Test
	public void testPassEmpty() throws Exception {
		SocketAddress socketAddress = AddressUtils.toSocketAddress("");
		
		Assert.assertNull(socketAddress);
	}

	@Test
	public void testPassNull() throws Exception {
		SocketAddress socketAddress = AddressUtils.toSocketAddress(null);
		
		Assert.assertNull(socketAddress);
	}
	
	@Test
	public void testPassLocalhost() throws Exception {
		SocketAddress socketAddress = AddressUtils.toSocketAddress("127.1");
		
		Assert.assertNotNull(socketAddress);
	}

	@Test
	public void testPass() throws Exception {
		SocketAddress socketAddress = AddressUtils.toSocketAddress("11.22.33.44");
		
		Assert.assertNotNull(socketAddress);
	}

	@Test(expected=UnknownHostException.class)
	public void testFailHostname() throws Exception {
		AddressUtils.toSocketAddress("abc");
	}

	@Test(expected=UnknownHostException.class)
	public void testFailNonDigits() throws Exception {
		AddressUtils.toSocketAddress("a.b.c.d");
	}

	@Test
	public void testPass3() throws Exception {
		SocketAddress socketAddress = AddressUtils.toSocketAddress("11.22.33");
		
		Assert.assertNotNull(socketAddress);
	}

	@Test(expected=UnknownHostException.class)
	public void testFailShort2() throws Exception {
		AddressUtils.toSocketAddress("11.22.33.");
	}

}
