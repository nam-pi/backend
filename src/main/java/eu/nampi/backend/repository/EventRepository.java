package eu.nampi.backend.repository;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "events")
public class EventRepository extends AbstractHydraRepository {

  public Model findAll(QueryParameters params) {
    WhereBuilder where = new WhereBuilder();
    // @formatter:off
    where
      .addWhere("?event", RDF.type, Core.event)
      .addWhere("?event", RDFS.label, "?label")
      .addOptional(new WhereBuilder()
        .addWhere("?event", Core.takesPlaceOn, "?exactDate")
        .addWhere("?exactDate", Core.hasXsdDateTime, "?exactDateTime"))
      .addOptional(new WhereBuilder()
        .addWhere("?event", Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addWhere("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime"))
      .addOptional(new WhereBuilder()
        .addWhere("?event", Core.takesPlaceNotLaterThan, "?latestDate")
        .addWhere("?latestDate", Core.hasXsdDateTime, "?latestDateTime"))
      .addOptional(new WhereBuilder()
        .addWhere("?event", Core.hasSortingDate, "?sortingDate")
        .addWhere("?sortingDate", Core.hasXsdDateTime, "?sortingDateTime"))
      .addBind(where.makeExpr("IF ( BOUND ( ?sortingDate ), ?sortingDate, IF ( BOUND ( ?exactDate ), ?exactDate, IF ( BOUND ( ?earliestDate ), ?earliestDate, IF ( BOUND ( ?latestDate ), ?latestDate, bnode() ) ) ) )"), "?realSortingDate")
      .addBind(where.makeExpr("IF ( BOUND ( ?sortingDateTime ), ?sortingDateTime, IF ( BOUND ( ?exactDateTime ), ?exactDateTime, IF ( BOUND ( ?earliestDateTime ), ?earliestDateTime, IF ( BOUND ( ?latestDateTime ), ?latestDateTime, '" + (params.getOrderByClauses().getOrderFor("?date").orElse(Order.ASCENDING) == Order.ASCENDING ? "9999-12-31T23:59:59" : "-9999-01-01:00:00:00") + "' ) ) ) )"), "?date");
    ConstructBuilder construct = getHydraCollectionBuilder(params, where, "?event", Api.orderBy)
      .addConstruct("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
      .addConstruct("?event", Core.hasSortingDate, "?realSortingDate")
      .addConstruct("?event", Core.takesPlaceNotEarlierThan, "?earliestDate")
      .addConstruct("?event", Core.takesPlaceNotLaterThan, "?latestDate")
      .addConstruct("?event", Core.takesPlaceOn, "?exactDate")
      .addConstruct("?event", RDF.type, Core.event)
      .addConstruct("?event", RDFS.label, "?label")
      .addConstruct("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
      .addConstruct("?latestDate", Core.hasXsdDateTime, "?latestDateTime")
      .addConstruct("?realSortingDate", Core.hasXsdDateTime, "?date");
    // @formatter:on
    return jenaService.construct(construct);
  }

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses}")
  public String findAll(QueryParameters params, Lang lang) {
    Model model = findAll(params);
    return serialize(model, lang);
  }

}
