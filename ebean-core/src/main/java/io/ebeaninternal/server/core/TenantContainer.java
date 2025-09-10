package io.ebeaninternal.server.core;

import io.ebean.Database;
import io.ebean.DatabaseBuilder;
import io.ebean.config.ContainerConfig;
import io.ebean.service.SpiContainer;

/**
 * Default Server side implementation of ServerFactory.
 */
public class TenantContainer implements SpiContainer {
  private final SpiContainer spiContainer;

  public TenantContainer(ContainerConfig containerConfig) {
    spiContainer = new DefaultContainer(containerConfig, true);
  }

  @Override
  public Database createServer(DatabaseBuilder configuration) {
    return spiContainer.createServer(configuration);
  }

  @Override
  public Database createServer(String name) {
    return spiContainer.createServer(name);
  }

  @Override
  public void shutdown() {
    spiContainer.shutdown();
  }
}
