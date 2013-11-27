SocketIo4Netty
=======================

SocketIo4Netty is a simple Socket.IO Java server implementation based on Netty.

How to use
-----------------------

``` java
	SocketIOServer socketIoServer = new SocketIOServer();
	socketIoServer.setPort(5000);
	socketIoServer.setListener(new SocketIOAdapter() {
		public void onMessage(ISession session, String message) {
			System.out.println("Received message: " + message);
		}
	});
	socketIoServer.start();
```

For more examples, see [socketIo4Netty-examples](https://github.com/socketIo4Netty/socketIo4Netty-examples). 