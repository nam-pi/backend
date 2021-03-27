package eu.nampi.backend.repository;

import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.HydraBuilder;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "events")
public class EventRepository extends AbstractHydraRepository {

  public Model findAll(QueryParameters params) {
    // @formatter:off
    HydraBuilder hydra = new HydraBuilder(params, Core.event, Api.orderBy)
      .addOptional(new WhereBuilder()
        .addWhere(HydraBuilder.MAIN_SUBJ, Core.takesPlaceOn, "?exactDate")
        .addWhere("?exactDate", Core.hasXsdDateTime, "?exactDateTime"))
      .addOptional(new WhereBuilder()
        .addWhere(HydraBuilder.MAIN_SUBJ, Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addWhere("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime"))
      .addOptional(new WhereBuilder()
        .addWhere(HydraBuilder.MAIN_SUBJ, Core.takesPlaceNotLaterThan, "?latestDate")
        .addWhere("?latestDate", Core.hasXsdDateTime, "?latestDateTime"))
      .addOptional(new WhereBuilder()
        .addWhere(HydraBuilder.MAIN_SUBJ, Core.hasSortingDate, "?sortingDate")
        .addWhere("?sortingDate", Core.hasXsdDateTime, "?sortingDateTime"))
      .addBind(
        "IF ( BOUND ( ?sortingDate ), ?sortingDate, IF ( BOUND ( ?exactDate ), ?exactDate, IF ( BOUND ( ?earliestDate ), ?earliestDate, IF ( BOUND ( ?latestDate ), ?latestDate, bnode() ) ) ) )",
        "?realSortingDate")
      .addBind(
        "IF ( BOUND ( ?sortingDateTime ), ?sortingDateTime, IF ( BOUND ( ?exactDateTime ), ?exactDateTime, IF ( BOUND ( ?earliestDateTime ), ?earliestDateTime, IF ( BOUND ( ?latestDateTime ), ?latestDateTime, '" + (params.getOrderByClauses().getOrderFor("?date").orElse(Order.ASCENDING) == Order.ASCENDING ? "9999-12-31T23:59:59" : "-9999-01-01:00:00:00") + "' ) ) ) )",
        "?date")
      .addMainConstruct(Core.hasSortingDate, "?realSortingDate")
      .addMainConstruct(Core.takesPlaceNotEarlierThan, "?earliestDate")
      .addMainConstruct(Core.takesPlaceNotLaterThan, "?latestDate")
      .addMainConstruct(Core.takesPlaceOn, "?exactDate")
      .addConstruct("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
      .addConstruct("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
      .addConstruct("?latestDate", Core.hasXsdDateTime, "?latestDateTime")
      .addConstruct("?realSortingDate", Core.hasXsdDateTime, "?date");
    // @formatter:on
    return construct(hydra);
  }

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type}")
  public String findAll(QueryParameters params, Lang lang) {
    Model model = findAll(params);
    return serialize(model, lang);
  }

}
