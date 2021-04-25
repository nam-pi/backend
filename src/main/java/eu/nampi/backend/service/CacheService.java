package eu.nampi.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CacheService {

  @Autowired
  private CacheManager cacheManager;

  public void clear() {
    // All cache names need to be manually configured in application.properties to
    // be available in cacheManager.getCacheNames
    cacheManager.getCacheNames().stream()
        .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    log.debug("Cleared cache");
  }

}
