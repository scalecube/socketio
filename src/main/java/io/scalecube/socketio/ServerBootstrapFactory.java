package io.scalecube.socketio;

import io.netty.bootstrap.ServerBootstrap;

/**
 * @author Anton Kharenko
 */
public interface ServerBootstrapFactory {

  ServerBootstrap createServerBootstrap();

}
