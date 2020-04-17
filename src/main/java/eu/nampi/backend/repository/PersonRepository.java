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

  private static final String CONSTRUCT_PERSON_DETAILS = new StringBuilder()
      .append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>")
      .append("PREFIX data: <https://purl.org/nampi/data/>").append("CONSTRUCT {")
      .append("<data:$ID$> rdf:label ?label .").append("?s ?p data:$ID$ .").append("}").append("WHERE {")
      .append("<data:$ID$> rdf:label ?label .").append("?s ?p data:$ID$ .").append("}").toString();

  private static final String CONSTRUCT_PERSON_LIST = new StringBuilder()
      .append("PREFIX nampi: <https://purl.org/nampi/owl/core#>").append("CONSTRUCT {").append("?p a nampi:person .")
      .append("}").append("WHERE {").append("?p a nampi:person .").append("}").toString();

  public Model getPerson(UUID id) {
    return jenaHelper.constructCore(CONSTRUCT_PERSON_DETAILS.replace("$ID$", id.toString()), false);
  }

  public Model getPersons() {
    return jenaHelper.constructCore(CONSTRUCT_PERSON_LIST, false);
  }
}