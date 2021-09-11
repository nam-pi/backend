package eu.nampi.backend.repository;

import java.util.UUID;

import org.apache.jena.rdf.model.Resource;
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

  protected String individualsUri(Resource type) {
    return endpointUri(type.getLocalName());
  }

  protected String individualsUri(Resource type, UUID id) {
    return individualsUri(type) + "/" + id;
  }

  protected String newIndividualUri(Resource type) {
    return individualsUri(type, UUID.randomUUID());
  }

}
