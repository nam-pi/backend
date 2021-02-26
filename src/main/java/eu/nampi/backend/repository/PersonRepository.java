package eu.nampi.backend.repository;

import java.util.Map;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.CollectionMeta;
import eu.nampi.backend.model.OrderByClauses;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
public class PersonRepository extends AbstractRdfRepository {

  public static final String ORDER_CLAUSE_NAME_LABEL = "label";

  public Model findAll(CollectionMeta meta) {
    WhereBuilder where = new WhereBuilder().addWhere("?person", RDF.type, Core.person)
        .addWhere("?person", RDFS.label, "?label");
    OrderByClauses clauses = new OrderByClauses();
    for (Map.Entry<Object, Order> entry : meta.getOrderByClauses().toMap().entrySet()) {
      String key = (String) entry.getKey();
      Order value = entry.getValue();
      if (ORDER_CLAUSE_NAME_LABEL.equals(key)) {
        clauses.add("?label", value);
      }
    }
    clauses.add("?person");
    String query = getHydraCollectionBuilder(meta, where, "?person", clauses, Api.orderBy)
        .addConstruct("?person", RDF.type, "core:person")
        .addConstruct("?person", RDFS.label, "?label").buildString();
    return jenaService.construct(query, true);
  }

}
