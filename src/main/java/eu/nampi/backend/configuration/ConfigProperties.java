package eu.nampi.backend.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "nampi")
public class ConfigProperties {

  String tripleStoreUrl;

  String coreOwlUrl;

}
