package eu.nampi.backend.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "nampi")
public class ConfigProperties {

  String tripleStoreUrl;

  String coreOwlUrl;

  List<String> otherOwlUrls;

}
