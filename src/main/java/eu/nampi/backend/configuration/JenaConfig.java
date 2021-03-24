package eu.nampi.backend.configuration;

import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.nampi.backend.service.FusekiService;
import eu.nampi.backend.service.JenaService;

@Configuration
public class JenaConfig {

  @Value("${nampi.triple-store-url}")
  private String tripleStoreUrl;

  @Bean
  public RDFConnectionRemoteBuilder getConnectionBuilder() {
    return RDFConnectionFuseki.create().destination(tripleStoreUrl);
  }

  @Bean
  public JenaService getJenaService() {
    return new FusekiService();
  }

}
