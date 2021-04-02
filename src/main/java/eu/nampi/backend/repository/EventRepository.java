package eu.nampi.backend.repository;

import static eu.nampi.backend.model.HydraCollectionBuilder.MAIN_SUBJ;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.converter.StringToDateRangeConverter;
import eu.nampi.backend.model.HydraCollectionBuilder;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "events")
public class EventRepository extends AbstractHydraRepository {

  private static final StringToDateRangeConverter CONVERTER = new StringToDateRangeConverter();

  public Model findAll(QueryParameters params, Optional<String> dates, Optional<String> statusType,
      Optional<String> occupationType, Optional<String> interactionType, Optional<String> participant) {
    HydraCollectionBuilder hydra = new HydraCollectionBuilder(params, Core.event, Api.eventOrderByVar);
    // @formatter:off
    interactionType.ifPresentOrElse(it -> hydra
        .addMainWhere("<" + it + ">", "?p")
        .addUnions(
          new WhereBuilder().addWhere("?p", RDF.type, Core.person), 
          new WhereBuilder().addWhere("?p", RDF.type, Core.group))
        .addSearchVariable("interactionType", Api.eventInteractionTypeVar, false, "'" + it + "'")
      , () -> hydra
        .addSearchVariable("interactionType", Api.eventInteractionTypeVar, false));
    participant.ifPresentOrElse(p -> {
      hydra.addMainWhere(Core.hasParticipant, "<" + p + ">").addSearchVariable("participant", Api.eventParticipantVar,
          false, "'" + p + "'");
    }, () -> {
      hydra.addSearchVariable("participant", Api.eventParticipantVar, false);
    });
    statusType.ifPresentOrElse(st -> {
      hydra
          .addMainWhere(PathFactory.pathSeq(PathFactory.pathLink(Core.usesStatus.asNode()),
              PathFactory.pathLink(RDF.type.asNode())), "<" + st + ">")
          .addSearchVariable("statusType", Api.eventStatusTypeVar, false, "'" + st + "'");
    }, () -> {
      hydra.addSearchVariable("statusType", Api.eventStatusTypeVar, false);
    });
    occupationType.ifPresentOrElse(ot -> {
      hydra
          .addMainWhere(PathFactory.pathSeq(PathFactory.pathLink(Core.usesOccupation.asNode()),
              PathFactory.pathLink(RDF.type.asNode())), "<" + ot + ">")
          .addSearchVariable("occupationType", Api.eventOccupationTypeVar, false, "'" + ot + "'");
    }, () -> {
      hydra.addSearchVariable("occupationType", Api.eventOccupationTypeVar, false);
    });
    hydra.addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceOn, "?exactDate")
        .addWhere("?exactDate", Core.hasXsdDateTime, "?exactDateTime"))
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addWhere("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime"))
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceNotLaterThan, "?latestDate")
        .addWhere("?latestDate", Core.hasXsdDateTime, "?latestDateTime"))
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.hasSortingDate, "?sortingDate")
        .addWhere("?sortingDate", Core.hasXsdDateTime, "?sortingDateTime"))
      .addBind( "if(bound(?sortingDate), ?sortingDate, if(bound(?exactDate), ?exactDate, if(bound(?earliestDate), ?earliestDate, if(bound(?latestDate), ?latestDate, bnode()))))", "?realSortingDate")
      .addBind( "if(bound(?sortingDateTime), ?sortingDateTime, if(bound(?exactDateTime), ?exactDateTime, if(bound(?earliestDateTime), ?earliestDateTime, if(bound(?latestDateTime), ?latestDateTime, '" + (params.getOrderByClauses().getOrderFor("date").orElse(Order.ASCENDING) == Order.ASCENDING ? "9999-12-31T23:59:59" : "-9999-01-01:00:00:00") + "'^^xsd:dateTime))))", "?date")
      .addMainConstruct(Core.hasSortingDate, "?realSortingDate")
      .addMainConstruct(Core.takesPlaceNotEarlierThan, "?earliestDate")
      .addMainConstruct(Core.takesPlaceNotLaterThan, "?latestDate")
      .addMainConstruct(Core.takesPlaceOn, "?exactDate")
      .addConstruct("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
      .addConstruct("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
      .addConstruct("?latestDate", Core.hasXsdDateTime, "?latestDateTime")
      .addConstruct("?realSortingDate", Core.hasXsdDateTime, "?date");
    // @formatter:on
    dates.map(s -> CONVERTER.convert(dates.get())).ifPresentOrElse(dr -> {
      hydra.addSearchVariable("dates", Api.eventDatesVar, false, "'" + dates.get() + "'");
      Optional<LocalDateTime> start = dr.getStart();
      if (start.isPresent()) {
        hydra.addBind("'" + start.get().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "'^^xsd:dateTime",
            "?filterStart");
      }
      Optional<LocalDateTime> end = dr.getEnd();
      if (end.isPresent()) {
        hydra.addBind("'" + end.get().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "'^^xsd:dateTime", "?filterEnd");
      }
      if (start.isPresent() && end.isPresent()) {
        hydra.addFilter("?date >= ?filterStart && ?date <= ?filterEnd");
      } else if (start.isPresent() && dr.isRange()) {
        hydra.addFilter("?date >= ?filterStart");
      } else if (start.isPresent()) {
        hydra.addFilter("?date = ?filterStart");
      } else {
        hydra.addFilter("?date <= ?filterEnd");
      }
    }, () -> {
      hydra.addSearchVariable("dates", Api.eventDatesVar, false);
    });
    return construct(hydra);
  }

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #dates, #statusType, #occupationType, #interactionType, #participant}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> dates, Optional<String> statusType,
      Optional<String> occupationType, Optional<String> interactionType, Optional<String> participant) {
    Model model = findAll(params, dates, statusType, occupationType, interactionType, participant);
    return serialize(model, lang);
  }

}
