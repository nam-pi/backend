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
    WhereBuilder where = new WhereBuilder();
    where.addWhere("?event", RDF.type, Core.event).addWhere("?event", RDFS.label, "?label")
        .addOptional(new WhereBuilder().addWhere("?event", Core.takesPlaceOn, "?exactDate")
            .addWhere("?exactDate", Core.hasXsdDateTime, "?exactDateTime"))
        .addOptional(
            new WhereBuilder().addWhere("?event", Core.takesPlaceNotEarlierThan, "?earliestDate")
                .addWhere("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime"))
        .addOptional(
            new WhereBuilder().addWhere("?event", Core.takesPlaceNotLaterThan, "?latestDate")
                .addWhere("?latestDate", Core.hasXsdDateTime, "?latestDateTime"))
        .addOptional(new WhereBuilder().addWhere("?event", Core.hasSortingDate, "?sortingDate")
            .addWhere("?sortingDate", Core.hasXsdDateTime, "?sortingDateTime"))
        .addBind(where.makeExpr(
            "IF ( BOUND ( ?sortingDate ), ?sortingDate, IF ( BOUND ( ?exactDate ), ?exactDate, IF ( BOUND ( ?earliestDate ), ?earliestDate, IF ( BOUND ( ?latestDate ), ?latestDate, bnode() ) ) ) )"),
            "?realSortingDate")
        .addBind(where.makeExpr(
            "IF ( BOUND ( ?sortingDateTime ), ?sortingDateTime, IF ( BOUND ( ?exactDateTime ), ?exactDateTime, IF ( BOUND ( ?earliestDateTime ), ?earliestDateTime, IF ( BOUND ( ?latestDateTime ), ?latestDateTime, '2100-12-31T23:59:59' ) ) ) )"),
            "?realSortingDateTime");
    String query = getHydraCollectionBuilder(meta, where, "?event", "?realSortingDateTime")
        .addConstruct("?event", RDF.type, Core.event).addConstruct("?event", RDFS.label, "?label")
        .addConstruct("?event", Core.hasSortingDate, "?realSortingDate")
        .addConstruct("?realSortingDate", Core.hasXsdDateTime, "?realSortingDateTime")
        .addConstruct("?event", Core.takesPlaceOn, "?exactDate")
        .addConstruct("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
        .addConstruct("?event", Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addConstruct("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
        .addConstruct("?event", Core.takesPlaceNotLaterThan, "?latestDate")
        .addConstruct("?latestDate", Core.hasXsdDateTime, "?latestDateTime").buildString();
    System.out.println(query);
    return jenaService.construct(query, true);
  }

}
