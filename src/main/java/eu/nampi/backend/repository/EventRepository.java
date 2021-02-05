package eu.nampi.backend.repository;

import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.CollectionMeta;
import eu.nampi.backend.vocabulary.Core;

@Repository
public class EventRepository extends AbstractRdfRepository {

  public Model findAll(CollectionMeta meta) {
    WhereBuilder where = new WhereBuilder().addWhere("?event", RDF.type, Core.event)
        .addWhere("?event", RDFS.label, "?label")
        .addOptional(new WhereBuilder().addWhere("?event", Core.takesPlaceOn, "?exactDate")
            .addWhere("?exactDate", Core.hasXsdDateTime, "?exactDateTime"))
        .addOptional(
            new WhereBuilder().addWhere("?event", Core.takesPlaceNotEarlierThan, "?earliestDate")
                .addWhere("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime"))
        .addOptional(
            new WhereBuilder().addWhere("?event", Core.takesPlaceNotLaterThan, "?latestDate")
                .addWhere("?latestDate", Core.hasXsdDateTime, "?latestDateTime"));
    String query = getHydraCollectionBuilder(meta, where, "?event", "event")
        .addConstruct("?event", RDF.type, Core.event).addConstruct("?event", RDFS.label, "?label")
        .addConstruct("?event", Core.takesPlaceOn, "?exactDate")
        .addConstruct("?event", Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addConstruct("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
        .addConstruct("?event", Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addConstruct("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
        .addConstruct("?event", Core.takesPlaceNotLaterThan, "?latestDate")
        .addConstruct("?latestDate", Core.hasXsdDateTime, "?latestDateTime").buildString();
    return jenaService.construct(query, true);
  }

}
