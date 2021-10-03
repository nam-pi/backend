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

  @Value("${nampi.dataset-url-data}")
  private String datasetUrlData;

  @Value("${nampi.dataset-url-inf-cache}")
  private String datasetUrlInfCache;

  @Bean
  public JenaService getJenaService() {
    RDFConnectionRemoteBuilder dataBuilder = RDFConnectionFuseki
        .create()
        .destination(datasetUrlData);
    RDFConnectionRemoteBuilder infCacheBuilder = RDFConnectionFuseki
        .create()
        .destination(datasetUrlInfCache);
    return new FusekiService(dataBuilder, infCacheBuilder);
  }
}
