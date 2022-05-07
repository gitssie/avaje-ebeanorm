package main;

import io.ebean.docker.commands.MariaDBContainer;

public class StartMariaDb {

  public static void main(String[] args) {
    MariaDBContainer.builder("10.5")
      .dbName("unit")
      .user("unit")
      .password("unit")
      .build()
      .startWithDropCreate();
  }
}
