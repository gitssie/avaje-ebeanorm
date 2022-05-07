package main;

import io.ebean.docker.commands.CockroachContainer;

public class StartCockroach {

  public static void main(String[] args) {
    CockroachContainer.builder("v21.2.4")
      .dbName("unit")
      .build()
      .start();
  }
}
