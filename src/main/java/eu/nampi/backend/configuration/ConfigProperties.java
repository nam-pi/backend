package eu.nampi.backend.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nampi")
public class ConfigProperties {

  String tripleStoreUrl;

  String coreOwlUrl;

  public String getTripleStoreUrl() {
    return tripleStoreUrl;
  }

  public void setTripleStoreUrl(String tripleStoreUrl) {
    this.tripleStoreUrl = tripleStoreUrl;
  }

  public String getCoreOwlUrl() {
    return coreOwlUrl;
  }

  public void setCoreOwlUrl(String coreOwlUrl) {
    this.coreOwlUrl = coreOwlUrl;
  }

}
