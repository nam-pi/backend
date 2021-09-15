package eu.nampi.backend.repository;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import eu.nampi.backend.service.JenaService;

public abstract class AbstractHydraRepository {

  @Value("${nampi.data-base-url}")
  private String dataBaseUrl;

  @Autowired
  protected JenaService jenaService;

  protected String endpointUri() {
    return dataBaseUrl == null || dataBaseUrl.isBlank()
        ? ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        : dataBaseUrl.replaceAll("/$", "");
  }

  protected String endpointUri(String... path) {
    StringBuilder builder = new StringBuilder(endpointUri());
    for (String string : path) {
      builder.append("/").append(string);
    }
    return builder.toString();
  }

  protected String endpointUri(String endpointName, UUID id) {
    return endpointUri(endpointName, id.toString());
  }
}
