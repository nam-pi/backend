package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.util.JenaUtils;

@Repository
public class EventRepository {

  @Autowired
  JenaUtils jenaHelper;

  private static final String CONSTRUCT_EVENT_LIST = """
      PREFIX nampi:<https://purl.org/nampi/owl/core#>
      CONSTRUCT {
        ?p a nampi:event .
      } WHERE {
        ?p a nampi:event .
      }
      """;

  public Model getEvents() {
    return jenaHelper.constructCore(CONSTRUCT_EVENT_LIST, true);
  }
}
