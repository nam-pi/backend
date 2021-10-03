package eu.nampi.backend.util;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class UrlBuilder {

  @Value("${nampi.data-base-url}")
  private String dataBaseUrl;

  public String endpointUri() {
    return dataBaseUrl == null || dataBaseUrl.isBlank()
        ? ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        : dataBaseUrl.replaceAll("/$", "");
  }

  public String endpointUri(String... path) {
    StringBuilder builder = new StringBuilder(endpointUri());
    for (String string : path) {
      builder.append("/").append(string);
    }
    return builder.toString();
  }

  public String endpointUri(String endpointName, UUID id) {
    return endpointUri(endpointName, id.toString());
  }

}
