package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.MAIN_SUBJ;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.converter.StringToDateRangeConverter;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;

@Repository
@CacheConfig(cacheNames = "events")
public class EventRepository extends AbstractHydraRepository {

  private static final StringToDateRangeConverter CONVERTER = new StringToDateRangeConverter();

  public Model findAll(QueryParameters params, Optional<String> dates, Optional<String> aspect,
      Optional<String> aspectType, Optional<String> aspectUseType, Optional<String> participant,
      Optional<String> participantType, Optional<String> participationType,
      Optional<String> place) {
    HydraCollectionBuilder hydra =
        new HydraCollectionBuilder(params, Core.event, Doc.eventOrderByVar);
    // @formatter:off
    place.ifPresentOrElse(pl -> hydra
        .addMainWhere(Core.takesPlaceAt, "<" + pl + ">")
        .addSearchVariable("place", Doc.eventParticipantVar, false, "'" + pl + "'")
      , () -> hydra
        .addSearchVariable("place", Doc.eventParticipantVar, false));
    participant.ifPresentOrElse(p -> hydra
        .addMainWhere(Core.hasParticipant, "<" + p + ">")
        .addSearchVariable("participant", Doc.eventParticipantVar, false, "'" + p + "'")
      , () -> hydra
        .addSearchVariable("participant", Doc.eventParticipantVar, false));
    participantType.ifPresentOrElse(pt -> hydra
        .addMainWhere(PathFactory.pathSeq(PathFactory.pathLink(Core.hasParticipant.asNode()), PathFactory.pathLink(RDF.type.asNode())), "<" + pt + ">")
        .addSearchVariable("participantType", Doc.eventParticipantTypeVar, false, "'" + pt + "'")
      , () -> hydra
        .addSearchVariable("participantType", Doc.eventParticipantTypeVar, false));
    participationType.ifPresentOrElse(pt -> hydra
        .addMainWhere("<" + pt + ">", "?p")
        .addWhere("?p", RDF.type, Core.agent)
        .addSearchVariable("participationType", Doc.eventParticipationTypeVar, false, "'" + pt + "'")
      , () -> hydra
        .addSearchVariable("participationType", Doc.eventParticipationTypeVar, false));
    aspect.ifPresentOrElse(a -> hydra
        .addMainWhere(Core.usesAspect, "<" + a + ">")
        .addSearchVariable("aspect", Doc.eventAspectVar, false, "'" + a + "'")
      , () -> hydra
        .addSearchVariable("aspect", Doc.eventAspectVar, false));
    aspectType.ifPresentOrElse(at -> hydra
        .addMainWhere(PathFactory.pathSeq(PathFactory.pathLink(Core.usesAspect.asNode()), PathFactory.pathLink(RDF.type.asNode())), "<" + at + ">")
        .addSearchVariable("aspectType", Doc.eventAspectTypeVar, false, "'" + at + "'")
      , () -> hydra
        .addSearchVariable("aspectType", Doc.eventAspectTypeVar, false));
    aspectUseType.ifPresentOrElse(aut -> hydra
        .addMainWhere(PathFactory.pathSeq(PathFactory.pathLink(ResourceFactory.createProperty(aut).asNode()), PathFactory.pathLink(RDF.type.asNode())), Core.aspect)
        .addSearchVariable("aspectUseType", Doc.eventAspectUseTypeVar, false, "'" + aut + "'")
      , () -> hydra
        .addSearchVariable("aspectUseType", Doc.eventAspectUseTypeVar, false));
    hydra
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceOn, "?exactDate")
        .addWhere("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
        .addWhere("?exactDate", RDF.type, Core.date))
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addWhere("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
        .addWhere("?earliestDate", RDF.type, Core.date))
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceNotLaterThan, "?latestDate")
        .addWhere("?latestDate", Core.hasXsdDateTime, "?latestDateTime")
        .addWhere("?latestDate", RDF.type, Core.date))
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.hasSortingDate, "?sortingDate")
        .addWhere("?sortingDate", Core.hasXsdDateTime, "?sortingDateTime")
        .addWhere("?sortingDate", RDF.type, Core.date))
      .addBind( "if(bound(?sortingDate), ?sortingDate, if(bound(?exactDate), ?exactDate, if(bound(?earliestDate), ?earliestDate, if(bound(?latestDate), ?latestDate, bnode()))))", "?realSortingDate")
      .addBind( "if(bound(?sortingDateTime), ?sortingDateTime, if(bound(?exactDateTime), ?exactDateTime, if(bound(?earliestDateTime), ?earliestDateTime, if(bound(?latestDateTime), ?latestDateTime, '" + (params.getOrderByClauses().getOrderFor("date").orElse(Order.ASCENDING) == Order.ASCENDING ? "9999-12-31T23:59:59" : "-9999-01-01:00:00:00") + "'^^xsd:dateTime))))", "?date")
      .addMainConstruct(Core.hasSortingDate, "?realSortingDate")
      .addMainConstruct(Core.takesPlaceNotEarlierThan, "?earliestDate")
      .addMainConstruct(Core.takesPlaceNotLaterThan, "?latestDate")
      .addMainConstruct(Core.takesPlaceOn, "?exactDate")
      .addConstruct("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
      .addConstruct("?earliestDate", RDF.type, Core.date)
      .addConstruct("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
      .addConstruct("?exactDate", RDF.type, Core.date)
      .addConstruct("?latestDate", Core.hasXsdDateTime, "?latestDateTime")
      .addConstruct("?latestDate", RDF.type, Core.date)
      .addConstruct("?realSortingDate", Core.hasXsdDateTime, "?date")
      .addConstruct("?realSortingDate", RDF.type, Core.date)
      ;
    // @formatter:on
    dates.map(s -> CONVERTER.convert(dates.get())).ifPresentOrElse(dr -> {
      hydra.addSearchVariable("dates", Doc.eventDatesVar, false, "'" + dates.get() + "'");
      Optional<LocalDateTime> start = dr.getStart();
      if (start.isPresent()) {
        hydra.addBind(
            "'" + start.get().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "'^^xsd:dateTime",
            "?filterStart");
      }
      Optional<LocalDateTime> end = dr.getEnd();
      if (end.isPresent()) {
        hydra.addBind(
            "'" + end.get().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "'^^xsd:dateTime",
            "?filterEnd");
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
      hydra.addSearchVariable("dates", Doc.eventDatesVar, false);
    });
    return construct(hydra);
  }

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #dates,#aspect, #aspectType, #aspectUseType, #participant, #participantType, #participationType, #place}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> dates,
      Optional<String> aspect, Optional<String> aspectType, Optional<String> aspectUseType,
      Optional<String> participant, Optional<String> participantType,
      Optional<String> participationType, Optional<String> place) {
    Model model = findAll(params, dates, aspect, aspectType, aspectUseType, participant,
        participantType, participationType, place);
    return serialize(model, lang, ResourceFactory.createResource(params.getBaseUrl()));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    String uri = individualsUri(Core.event, id);
    HydraSingleBuilder builder = new HydraSingleBuilder(uri, Core.event);
    builder
        .addOptional(new WhereBuilder().addWhere(MAIN_SUBJ, Core.takesPlaceOn, "?exactDate")
            .addWhere("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
            .addWhere("?exactDate", RDF.type, Core.date))
        .addOptional(
            new WhereBuilder().addWhere(MAIN_SUBJ, Core.takesPlaceNotEarlierThan, "?earliestDate")
                .addWhere("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
                .addWhere("?earliestDate", RDF.type, Core.date))
        .addOptional(
            new WhereBuilder().addWhere(MAIN_SUBJ, Core.takesPlaceNotLaterThan, "?latestDate")
                .addWhere("?latestDate", Core.hasXsdDateTime, "?latestDateTime")
                .addWhere("?latestDate", RDF.type, Core.date))
        .addMainConstruct(Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addMainConstruct(Core.takesPlaceNotLaterThan, "?latestDate")
        .addMainConstruct(Core.takesPlaceOn, "?exactDate")
        .addConstruct("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
        .addConstruct("?earliestDate", RDF.type, Core.date)
        .addConstruct("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
        .addConstruct("?exactDate", RDF.type, Core.date)
        .addConstruct("?latestDate", Core.hasXsdDateTime, "?latestDateTime")
        .addConstruct("?latestDate", RDF.type, Core.date);
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(uri));
  }

}
