package io.ebeaninternal.server.logger;

import io.ebeaninternal.api.SpiLogger;

import static java.lang.System.Logger.Level.DEBUG;

final class DSpiLogger implements SpiLogger {

  private final System.Logger logger;

  DSpiLogger(System.Logger logger) {
    this.logger = logger;
  }

  @Override
  public boolean isDebug() {
    return logger.isLoggable(DEBUG);
  }

  @Override
  public void debug(String msg, Object... args) {
    logger.log(DEBUG, msg, args);
  }
}
