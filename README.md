[![Build Status](https://travis-ci.org/scalecube/socketio.svg?branch=master)](https://travis-ci.org/scalecube/socketio)

Socket.IO Java Server
=======================
 
ScaleCube Socket.IO is a lightweight implementation of [Socket.IO](http://socket.io) Java server based on 
[Netty](http://netty.io) framework. It implements subset of Socket.IO protocol which is optimized for high 
throughput and low latency real-time messaging. Supports 0.7+ up to 0.9.16 versions of 
[Socket.IO-client](https://github.com/socketio/socket.io-client/tree/0.9). 

Supported transport protocols:
* WebSocket
* Flash Socket
* XHR-Polling
* JSONP-Polling

Performance
-----------------------
Tested on VM: CentOS, 4vCPU, 2GB RAM, Java 7

Client Sessions:
- 10,000 long-polling sessions on single node
- 50,000 WebSocket sessions on single node

TPS:
- 4,000 requests per second per single channel
- 80,000 requests per second total

How to use
-----------------------

``` java
  SocketIOServer socketIoServer = SocketIOServer.newInstance(5000 /*port*/);
  socketIoServer.setListener(new SocketIOAdapter() {
    public void onMessage(ISession session, ByteBuf message) {
      System.out.println("Received: " + message.toString(CharsetUtil.UTF_8));
      message.release();
    }
  });
  socketIoServer.start();
```

For more examples, see [Socket.IO Examples](https://github.com/scalecube/socketio-examples). 

Maven
---------------------- 

Binaries and dependency information for Maven can be found at 
[http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.scalecube%22%20AND%20a%3A%22socketio%22).

Change history and version numbers at [CHANGES.md](https://github.com/scalecube/socketio/blob/master/CHANGES.md).

Maven dependency: 

``` maven
<dependency>
  <groupId>io.scalecube</groupId>
  <artifactId>socketio</artifactId>
  <version>2.1.1</version>
</dependency>
```

Starting from version 2.0.1 Netty dependency is optional in order to allow change of Netty version independently. 
So following dependencies should be added to your project:

``` maven
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-buffer</artifactId>
  <version>4.0.33.Final</version>
</dependency>
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-common</artifactId>
  <version>4.0.33.Final</version>
</dependency>
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-handler</artifactId>
  <version>4.0.33.Final</version>
</dependency>
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-codec</artifactId>
  <version>4.0.33.Final</version>
</dependency>
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-codec-http</artifactId>
  <version>4.0.33.Final</version>
</dependency>
```

Maven dependency for versions up to 1.1.2:
 
``` maven
<dependency>
  <groupId>com.github.socketIo4Netty</groupId>
  <artifactId>socketIo4Netty</artifactId>
  <version>1.1.2</version>
</dependency>
``` 

Bugs and Feedback
----------------------

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/scalecube/socketio/issues).

License
----------------------

[Apache License, Version 2.0](https://github.com/scalecube/socketio/blob/master/LICENSE.txt)
