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
public class EventRepository extends AbstractRdfRepository {

  public static final String ORDER_CLAUSE_NAME_DATE = "date";

  public static final String ORDER_CLAUSE_NAME_LABEL = "label";

  public Model findAll(CollectionMeta meta) {
    WhereBuilder where = new WhereBuilder();
    OrderByClauses clauses = new OrderByClauses();
    for (Map.Entry<Object, Order> entry : meta.getOrderByClauses().toMap().entrySet()) {
      String key = (String) entry.getKey();
      Order value = entry.getValue();
      if (ORDER_CLAUSE_NAME_DATE.equals(key)) {
        clauses.add("?realSortingDateTime", value);
      } else if (ORDER_CLAUSE_NAME_LABEL.equals(key)) {
        clauses.add("?label", value);
      }
    }
    clauses.add("?event");
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
            "IF ( BOUND ( ?sortingDateTime ), ?sortingDateTime, IF ( BOUND ( ?exactDateTime ), ?exactDateTime, IF ( BOUND ( ?earliestDateTime ), ?earliestDateTime, IF ( BOUND ( ?latestDateTime ), ?latestDateTime, '"
                + (clauses.getOrderFor("?realSortingDateTime")
                    .orElse(Order.ASCENDING) == Order.ASCENDING ? "9999-12-31T23:59:59"
                        : "-9999-01-01:00:00:00")
                + "' ) ) ) )"),
            "?realSortingDateTime");
    String query = getHydraCollectionBuilder(meta, where, "?event", clauses, Api.orderBy)
        .addConstruct("?event", RDF.type, Core.event).addConstruct("?event", RDFS.label, "?label")
        .addConstruct("?event", Core.hasSortingDate, "?realSortingDate")
        .addConstruct("?realSortingDate", Core.hasXsdDateTime, "?realSortingDateTime")
        .addConstruct("?event", Core.takesPlaceOn, "?exactDate")
        .addConstruct("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
        .addConstruct("?event", Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addConstruct("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
        .addConstruct("?event", Core.takesPlaceNotLaterThan, "?latestDate")
        .addConstruct("?latestDate", Core.hasXsdDateTime, "?latestDateTime").buildString();
    return jenaService.construct(query, true);
  }

}
