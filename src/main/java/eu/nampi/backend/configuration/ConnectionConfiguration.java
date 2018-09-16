package eu.nampi.backend.configuration;

import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConnectionConfiguration {

    @Value("${nampi.fuseki_url}")
    private String url;

    @Bean
    public RDFConnectionRemoteBuilder rdfConnectionBuilder() {
        return RDFConnectionFuseki.create().destination(url);
    }

}