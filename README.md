# Socket.IO Java Server

[![Build Status](https://travis-ci.org/scalecube/socketio.svg?branch=master)](https://travis-ci.org/scalecube/socketio)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.scalecube/socketio/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.scalecube/socketio)
 
ScaleCube Socket.IO is a lightweight implementation of [Socket.IO](http://socket.io) Java server based on 
[Netty](http://netty.io) framework. It implements subset of Socket.IO protocol and optimized for high throughput 
and low latency real-time messaging. It is designed to support requirements of most demanding modern applications 
such as online gaming, financial trading, social and advertising platforms. Supports 0.7+ up to 0.9.16 versions of 
[Socket.IO-client](https://github.com/socketio/socket.io-client/tree/0.9). 

Socket.IO protocol provides WebSocket transport with fallback options to other transports such as XHR-Polling 
in case if client does not support or unable to establish WebSocket connection (e.g. due to proxy or firewall 
restrictions). It supports reconnection mechanism based on exponential backoff algorithm and heartbeat-based 
detection of disconnections.

ScaleCube Socket.IO is a lightweight embeddable library with minimum dependencies for the Java VM. Major use case
is to provide an implementation of transport layer for [API Gateway](http://microservices.io/patterns/apigateway.html) 
pattern in microservices architecture. Mobile and web clients use Socket.IO transport to communicate with application 
microservices.

Supported transport protocols:
* WebSocket
* Flash Socket
* XHR-Polling
* JSONP-Polling

## Performance

Tested on VM: CentOS, 4vCPU, 2GB RAM, Java 7

Client Sessions:
- 10,000 long-polling sessions on single node
- 50,000 WebSocket sessions on single node

TPS:
- 4,000 requests per second per single channel
- 80,000 requests per second total

## Getting Started

Start Socket.IO server on port `5000` which prints to console all received messages:

``` java
SocketIOServer server = SocketIOServer.newInstance(5000 /*port*/);
server.setListener(new SocketIOAdapter() {
  public void onMessage(ISession session, ByteBuf message) {
    System.out.println("Received: " + message.toString(CharsetUtil.UTF_8));
    message.release();
  }
});
server.start();
```

Start echo Socket.IO server:

``` java
SocketIOServer echoServer = SocketIOServer.newInstance(5000 /*port*/);
echoServer.setListener(new SocketIOAdapter() {
  public void onMessage(ISession session, ByteBuf message) {
    session.send(message);
  }
});
echoServer.start();
```

Note that received message has type of Netty's `ByteBuffer` since the popular use case are proxy-like applications it 
allows to resend received payload without decoding it. If byte buffer will be sent to another Netty channel it will 
be released automatically, otherwise it is required to manually release buffer.

For more examples and demo client application, see [Socket.IO Examples](https://github.com/scalecube/socketio-examples). 

## Maven 

Binaries and dependency information for Maven can be found at 
[http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.scalecube%22%20AND%20a%3A%22socketio%22).

Change history and version numbers at [CHANGES.md](https://github.com/scalecube/socketio/blob/master/CHANGES.md).

Maven dependency: 

``` xml
<dependency>
  <groupId>io.scalecube</groupId>
  <artifactId>socketio</artifactId>
  <version>x.y.z</version>
</dependency>
```

Netty dependency is made optional in order to allow change of Netty's minor version.
So following Netty 4.0.x modules should be added to your project with specified minor version, e.g.:

``` xml
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-buffer</artifactId>
  <version>4.0.36.Final</version>
</dependency>
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-common</artifactId>
  <version>4.0.36.Final</version>
</dependency>
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-handler</artifactId>
  <version>4.0.36.Final</version>
</dependency>
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-codec</artifactId>
  <version>4.0.36.Final</version>
</dependency>
<dependency>
  <groupId>io.netty</groupId>
  <artifactId>netty-codec-http</artifactId>
  <version>4.0.36.Final</version>
</dependency>
```

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/scalecube/socketio/issues).

## License

[Apache License, Version 2.0](https://github.com/scalecube/socketio/blob/master/LICENSE.txt)
