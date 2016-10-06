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

Start Socket.IO server on port `5000` which prints to console all received messages and connected/disconnected events:

``` java
SocketIOServer server = SocketIOServer.newInstance(5000 /*port*/);
server.setListener(new SocketIOListener() {
  public void onConnect(Session session) {
    System.out.println("Connected: " + session);  
  }
  
  public void onMessage(Session session, ByteBuf message) {
    System.out.println("Received: " + message.toString(CharsetUtil.UTF_8));
    message.release();
  }
  
  public void onDisconnect(Session session) {
    System.out.println("Disconnected: " + session);  
  }
});
server.start();
```

Start echo Socket.IO server as simple as:

``` java
SocketIOServer echoServer = SocketIOServer.newInstance(5000 /*port*/);
echoServer.setListener(new SocketIOAdapter() {
  public void onMessage(Session session, ByteBuf message) {
    session.send(message);
  }
});
echoServer.start();
```

Note that received message has type of Netty's [ByteBuffer](https://netty.io/4.0/api/io/netty/buffer/ByteBuf.html) 
since the popular use case are proxy-like applications it allows to resend received payload without decoding it. 
If byte buffer will be sent to another Netty channel it will be released automatically, otherwise it is required 
to manually release buffer.

Start Socket.IO server with SSL:

``` java
// Server config
SSLContext sslContext = ... // your server's SSL context 
ServerConfiguration configWithSsl = ServerConfiguration.builder()
    .port(5000)
    .sslContext(sslContext)
    .build();
    
// Start server
SocketIOServer sslServer = SocketIOServer.newInstance(configWithSsl);
sslServer.setListener(new SocketIOAdapter() { /* listener code */ });
sslServer.start();
```

To play with your Socket.IO server you may use our [demo client](http://scalecube.io/socketio/).   

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

## Server configuration

- *port*
  
  Port on which Socket.IO server will be started. Default value is `8080`.

- *sslContext*

  SSL context which is used to run secure socket. If it's set to `null` server runs without SSL.
  Default value is `null`.

- *transports*
  
  A string with list of allowed transport methods separated by comma.
  Default value is `"websocket,flashsocket,xhr-polling,jsonp-polling"`.

- *heartbeatTimeout*
  
  The timeout in seconds for the client when it should send a new heart
  beat to the server. This value is sent to the client after a successful
  handshake. The default value is `60`.

- *closeTimeout*
  
  The timeout in seconds for the client, when it closes the connection it
  still X amounts of seconds to do re open of the connection. This value is
  sent to the client after a successful handshake. Default value is `60`.

- *heartbeatInterval*

  The timeout in seconds for the server, we should receive a heartbeat from
  the client within this interval. This should be less than the heartbeat
  timeout. Default value is `25`.

- *eventExecutorEnabled*
  
  Flag which defines if listener will be executed, true - different thread, false - io-thread.
  Default is `true`.
  
- *eventExecutorThreadNumber*
  
  Event executor thread number, if eventExecutorEnabled flag set to true.
  Default value is `Runtime.getRuntime().availableProcessors() x 2`.

- *maxWebSocketFrameSize*
  
  Maximum allowable web socket frame payload length. Setting this value to your application's requirement may
  reduce denial of service attacks using long data frames. Default is `65536`.
  
- *alwaysSecureWebSocketLocation*
  
  Flag which if set to true will always return secure web socket location protocol ("wss://")
  even when connection is established over plain socket. It is used as a workaround related to case
  when SSL is offloaded to Load Balancer, but it doesn't modify web socket location. By default it
  is `false`.

- *remoteAddressHeader*
  
  The HTTP header name which is used as a session remote address. It is a workaround related to case
  when Load Balancer modify client address with its address. This header is supposed to be set by Load
  Balancer. If it is set to `null` then this header is not used. Default value is `null`.

## Bugs and Feedback

For bugs, questions and discussions please use the [GitHub Issues](https://github.com/scalecube/socketio/issues).

## License

[Apache License, Version 2.0](https://github.com/scalecube/socketio/blob/master/LICENSE.txt)
