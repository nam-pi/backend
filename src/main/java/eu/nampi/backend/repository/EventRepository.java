package eu.nampi.backend.repository;

import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.CollectionMeta;
import eu.nampi.backend.vocabulary.Core;

@Repository
public class EventRepository extends AbstractRdfRepository {

  public Model findAll(CollectionMeta meta) {
    WhereBuilder where = new WhereBuilder().addWhere("?event", RDF.type, Core.event);
    String query = getHydraCollectionBuilder(meta, where, "?event", "event")
        .addConstruct("?event", RDF.type, Core.event).buildString();
    return jenaService.construct(query, true);
  }

}
