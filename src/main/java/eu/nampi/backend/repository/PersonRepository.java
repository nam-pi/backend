package eu.nampi.backend.repository;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
public class PersonRepository extends AbstractHydraRepository {

  public Model findAll(QueryParameters params) {
    WhereBuilder where = new WhereBuilder().addWhere("?person", RDF.type, Core.person).addWhere("?person", RDFS.label,
        "?label");
    ConstructBuilder construct = getHydraCollectionBuilder(params, where, "?person", Api.orderBy)
        .addConstruct("?person", RDF.type, Core.person).addConstruct("?person", RDFS.label, "?label");
    return jenaService.construct(construct);
  }

  @Cacheable(value = "persons-find-all", key = "{#lang, #params.limit, #params.offset, #params.orderByClauses}")
  public String findAll(QueryParameters params, Lang lang) {
    Model model = findAll(params);
    return serialize(model, lang);
  }

}
