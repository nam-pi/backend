package eu.nampi.backend.repository;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.service.JenaService;

@Repository
public class PersonRepository {

  @Autowired
  JenaService sparql;

  private final ConstructBuilder findAllQueryBuilder =
      new ConstructBuilder().addPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
          .addPrefix("core", "https://purl.org/nampi/owl/core#")
          .addConstruct("?p", "rdf:type", "core:person").addWhere("?p", "rdf:type", "core:person")
          .addOrderBy("?p");

  public Model findAll(int limit, int offset) {
    return sparql.construct(findAllQueryBuilder.setLimit(limit).setOffset(offset).buildString(),
        true);
  }
}
