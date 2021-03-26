package eu.nampi.backend.tasks;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import eu.nampi.backend.service.JenaService;

@Component
public class Startup {

  @Autowired
  private CacheManager cacheManager;

  @Autowired
  private JenaService jenaService;

  @PostConstruct
  public void init() {
    evictCache();
    jenaService.initInfCache();
  }

  private void evictCache() {
    // All cache names need to be manually configured in application.properties to
    // be available in cacheManager.getCacheNames
    cacheManager.getCacheNames().stream().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
  }

}
