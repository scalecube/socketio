Changes
=======================

2.1.0
-----------------------
* Move project packages under io.scalecube.socketio
* Make configurable WebSocket max frame size
* Remove memoizer from SessionStorage

2.0.1 / 2015-09-10
-----------------------
* Upgrade netty to 4.0.31.Final

2.0.0 / 2015-06-16
-----------------------
* Move project packages under io.servicefabric.socketio

1.1.2 / 2015-06-12
-----------------------
* Remove dependency on Jackson library
* Update Netty to version 4.0.28.Final
* Ignore PongWebsocketFrame as not supported, change log level for IOException

1.1.1 / 2014-12-24
-----------------------
* Fix memory leak at PacketDecoder

1.1.0 / 2014-12-24
-----------------------
* Reduce GC and CPU overhead of packet encoding/decoding
* Remove returning future on `ISession` send operation feature to reduce GC overhead
* Expose and accept `ByteBuf` as external message class instead of `String`
* Significantly reduced GC activity and improved performance on big messages 

1.0.20 / 2014-11-07
-----------------------
* Update Netty to version 4.0.24
* Use PooledByteBufAllocator
* Use HashedWheelTimer for scheduling heartbeats and disconnection
* Use "heartbeatInterval" for scheduling heartbeats and "heartbeatTimeout" for disconnection 

1.0.19 / 2014-10-07
-----------------------
* Update Jackson to version 2.4.1

1.0.18 / 2014-08-29
-----------------------
* Hide stacktraces for IOExceptions caught in channel
* Make library osgi compatible with netty4
* Fix IllegalReferenceCountException on websocket disconnection
* Add Builder for server configuration

1.0.17 / 2014-06-23
-----------------------
* Added support of custom HTTP header for discovering client IP address

1.0.16 / 2014-06-13
-----------------------
* Migrate to netty 4

1.0.15 / 2014-02-07
-----------------------
* Added additional debug information in log

1.0.14 / 2014-01-13
-----------------------

* Added support of JSONP-Polling transport
* Return future on `ISession` send operation

1.0.12 / 2014-01-08
-----------------------

* Fix bug in `FlashPolicyHandler` over SSL connection 

1.0.11 / 2013-12-27
-----------------------

* Added support of Flash Socket transport
* Added `SocketIOAdapter` class

1.0.10 / 2013-11-24
-----------------------

Initial release.
