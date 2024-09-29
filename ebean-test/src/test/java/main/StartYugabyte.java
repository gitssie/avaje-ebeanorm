package main;

import io.ebean.test.containers.YugabyteContainer;

public class StartYugabyte {

  public static void main(String[] args) {
    YugabyteContainer.builder("2.11.2.0-b89")
      .dbName("unit")
      .user("unit")
      .extensions("pgcrypto")
      .build()
      .startWithDropCreate();

//    Run container ut_yugabyte with host:localhost port:6433 db:unit user:unit/test shutdown:None
//    docker run -d --name ut_yugabyte -p 6433:5433 -p 7000:7000 -p 9000:9000 -p 9042:9042 yugabytedb/yugabyte:2.11.2.0-b89 bin/yugabyted start --daemon=false
//    ...
//    Commands - sqlRun: drop database if exists unit
//    Commands - sqlRun: drop role if exists unit
//    Commands - sqlRun: select 1 from pg_database where datname = 'unit'
//    Commands - sqlRun: select rolname from pg_roles where rolname = 'unit'
//    Commands - sqlRun: create role unit password 'test' login createrole
//    Commands - sqlRun: create database unit with owner unit

  }
}
