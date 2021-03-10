package eu.nampi.backend.repository;

import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.CollectionMeta;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
public class PersonRepository extends AbstractRdfRepository {

  public Model findAll(CollectionMeta meta) {
    WhereBuilder where = new WhereBuilder().addWhere("?person", RDF.type, Core.person).addWhere("?person", RDFS.label,
        "?label");
    String query = getHydraCollectionBuilder(meta, where, "?person", meta.getOrderByClauses(), Api.orderBy)
        .addConstruct("?person", RDF.type, "core:person").addConstruct("?person", RDFS.label, "?label").buildString();
    return jenaService.construct(query, true);
  }
}
