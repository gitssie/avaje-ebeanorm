package io.ebeaninternal.server.cache;

import io.ebean.cache.ServerCacheConfig;
import io.ebean.cache.ServerCacheOptions;
import io.ebean.cache.ServerCacheStatistics;
import io.ebean.cache.ServerCacheType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Random;


class DefaultServerCache_RunEvictionTest {

  private DefaultServerCache createCache() {
    ServerCacheOptions cacheOptions = new ServerCacheOptions();
    cacheOptions.setMaxSize(300);
    cacheOptions.setMaxIdleSecs(1);
    cacheOptions.setMaxSecsToLive(2);
    cacheOptions.setTrimFrequency(5);

    ServerCacheConfig con = new ServerCacheConfig(ServerCacheType.BEAN, "foo", "foo", cacheOptions, null, null);
    return new DefaultServerCache(new DefaultServerCacheConfig(con));
  }

  private final DefaultServerCache cache;

  private final Random random = new Random();

  public DefaultServerCache_RunEvictionTest() {
    this.cache = createCache();
  }

  @Disabled("test takes long time")
  @Test
  void runEvict() throws InterruptedException {
    for (int i = 0; i < 15; i++) {
      doStuff();
      cache.runEviction();
      ServerCacheStatistics statistics = cache.statistics(true);
      System.out.println(statistics);
      Thread.sleep(500);
    }
  }

  private void doStuff() {
    for (int i = 0; i < 5000; i++) {
      String key = "" + random.nextInt(20000);
      int mode = random.nextInt(10);
      if (mode < 7) {
        cache.get(key);
      } else {
        cache.put(key, key + "-" + System.currentTimeMillis());
      }
    }
  }
}
