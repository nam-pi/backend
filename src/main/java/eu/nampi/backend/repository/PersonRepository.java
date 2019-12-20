package eu.nampi.backend.repository;

import java.util.UUID;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.util.JenaUtils;

@Repository
public class PersonRepository {

  @Autowired
  JenaUtils jenaHelper;

  private static final String CONSTRUCT_PERSON_DETAILS = """
      PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      PREFIX data: <https://purl.org/nampi/data/>
      CONSTRUCT {
          <data:$ID$> rdf:label ?label .
          ?s ?p data:$ID$ .
      }
      WHERE {
          <data:$ID$> rdf:label ?label .
          ?s ?p data:$ID$ .
      }
      """;

  private static final String CONSTRUCT_PERSON_LIST = """
      PREFIX nampi: <https://purl.org/nampi/owl/core#>
      CONSTRUCT {
          ?p a nampi:person .
      }
      WHERE {
          ?p a nampi:person .
      }
      """;

  public Model getPerson(UUID id) {
    return jenaHelper.constructCore(CONSTRUCT_PERSON_DETAILS.replace("$ID$", id.toString()), false);
  }

  public Model getPersons() {
    return jenaHelper.constructCore(CONSTRUCT_PERSON_LIST, false);
  }
}