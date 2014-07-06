[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/0d069b0ed5626821f3611b4c229bbe58 "githalytics.com")](http://githalytics.com/socketIo4Netty/socketIo4j)

SocketIo4j
=======================

Performance: 
Tested on VM: OS: CentOS, 4vCPU, 2GB RAM,Java 7

Client Sessions:

can support:   
- 10,000 long-polling sessions on single node
- 50,000 WebSocket    sessions on single node

TPS:
- 4,000 requests per second per single channel.
- 80,000 requests per second total. 

SocketIo4Netty is a simple [Socket.IO](http://socket.io) Java server implementation based on 
[Netty](http://netty.io) server framework. Supports 0.7+ up to latest 0.9.16 versions of 
Socket.IO-client.

Supported transport protocols:
* WebSocket
* Flash Socket
* XHR-Polling
* JSONP-Polling

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

For more examples, see [SocketIo4j Examples](https://github.com/socketIo4Netty/socketIo4Netty-examples). 

Maven
----------------------

``` maven
<dependency>
	<groupId>com.github.socketIo4Netty</groupId>
	<artifactId>socketIo4Netty</artifactId>
	<version>1.0.16</version>
</dependency>
```
