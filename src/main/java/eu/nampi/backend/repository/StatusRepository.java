package eu.nampi.backend.repository;

import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
public class StatusRepository extends AbstractHydraRepository {

  public Model findAll(QueryParameters params) {
    WhereBuilder where = new WhereBuilder().addWhere("?status", RDF.type, Core.status).addWhere("?status", RDFS.label,
        "?label");
    String query = getHydraCollectionBuilder(params, where, "?status", Api.orderBy)
        .addConstruct("?person", RDF.type, Core.status).addConstruct("?status", RDFS.label, "?label").buildString();
    return jenaService.construct(query);
  }
}
