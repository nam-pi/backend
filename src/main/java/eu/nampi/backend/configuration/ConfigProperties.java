package eu.nampi.backend.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "nampi")
public class ConfigProperties {

  String coreOwlUrl;

  String datasetUrlData;

  String datasetUrlInfCache;

  String dataBaseUrl;

  int defaultLimit;

  String crmPrefix;

  String keycloakRdfIdAttribute;

  List<String> otherOwlUrls;
}
