package eu.nampi.backend.repository;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.converter.StringToDateRangeConverter;
import eu.nampi.backend.model.DateRange;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "events")
public class EventRepository extends AbstractHydraRepository {

  private static final StringToDateRangeConverter CONVERTER = new StringToDateRangeConverter();

  private static final Node VAR_ASPECT = NodeFactory.createVariable("aspect");
  private static final Node VAR_ASPECT_LABEL = NodeFactory.createVariable("aspectLabel");
  private static final Node VAR_ASPECT_STRING = NodeFactory.createVariable("aspectString");
  private static final Node VAR_DATE = NodeFactory.createVariable("date");
  private static final Node VAR_DATE_EARLIEST = NodeFactory.createVariable("dateEarliest");
  private static final Node VAR_DATE_EXACT = NodeFactory.createVariable("dateExact");
  private static final Node VAR_DATE_LATEST = NodeFactory.createVariable("dateLatest");
  private static final Node VAR_DATE_REAL_SORT = NodeFactory.createVariable("dateRealSort");
  private static final Node VAR_DATE_SORT = NodeFactory.createVariable("dateSort");
  private static final Node VAR_DATE_TIME_EARLIEST = NodeFactory.createVariable("dateTimeEarliest");
  private static final Node VAR_DATE_TIME_EXACT = NodeFactory.createVariable("dateTimeExact");
  private static final Node VAR_DATE_TIME_LATEST = NodeFactory.createVariable("dateTimeLatest");
  private static final Node VAR_DATE_TIME_SORT = NodeFactory.createVariable("dateTimeSort");
  private static final Node VAR_PARTICIPANT = NodeFactory.createVariable("participant");
  private static final Node VAR_PARTICIPANT_LABEL = NodeFactory.createVariable("participantLabel");
  private static final Node VAR_PLACE = NodeFactory.createVariable("place");
  private static final Node VAR_PLACE_LABEL = NodeFactory.createVariable("placeLabel");

  public Model findAll(QueryParameters params, Optional<String> dates, Optional<String> aspect,
      Optional<String> aspectType, Optional<String> aspectUseType, Optional<String> participant,
      Optional<String> participantType, Optional<String> participationType,
      Optional<String> place) {

    HydraCollectionBuilder builder =
        new HydraCollectionBuilder(endpointUri("events"), Core.event, Api.eventOrderByVar, params);
    ExprFactory ef = builder.getExprFactory();
    Node varMain = HydraCollectionBuilder.VAR_MAIN;

  // @formatter:off
    // Get custom queries
    WhereBuilder aspectWhere = aspectWhere(varMain);
    WhereBuilder datesWhere = datesWhere(varMain, ef,Optional.of(params));
    WhereBuilder participantWhere = participantWhere(varMain);
    WhereBuilder placeWhere = placeWhere(varMain);

    /* Add query data */

    // Participant data
    builder.dataWhere.addWhere(participantWhere);
    if(participant.isPresent()) {
      Node varParticipant = NodeFactory.createVariable("filterParticipant");
      Resource participantResource = ResourceFactory.createResource(participant.get());
      Expr sameTerm = ef.sameTerm(varParticipant, participantResource);
      builder.dataWhere
        .addWhere(varMain, Core.hasParticipant, varParticipant)
        .addFilter(sameTerm);
      builder.countWhere
        .addWhere(varMain, Core.hasParticipant, varParticipant)
        .addFilter(sameTerm);
    }
    if(participantType.isPresent()) {
      Node varParticipant = NodeFactory.createVariable("filterParticipantTypeParticipant");
      Resource typeResource = ResourceFactory.createResource(participantType.get());
      builder.dataWhere
        .addWhere(varMain, Core.hasParticipant, varParticipant)
        .addWhere(varParticipant, RDF.type, typeResource);
      builder.countWhere
        .addWhere(varMain, Core.hasParticipant, varParticipant)
        .addWhere(varParticipant, RDF.type, typeResource);
    }
    if(participationType.isPresent()) {
      Node varType = NodeFactory.createVariable("filterParticipationType");
      Property type = ResourceFactory.createProperty(participationType.get());
      builder.dataWhere
        .addWhere(varMain, type, varType);
      builder.countWhere
        .addWhere(varMain, type, varType);
    }

    // Dates data
    builder.dataWhere.addWhere(datesWhere);
    if (dates.isPresent()) {
      String datesString = dates.get();
      DateRange dateRange = CONVERTER.convert(datesString);
      Optional<String> start = dateRange.getStart().map(date -> "'" + date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "'^^xsd:dateTime");
      Optional<String> end = dateRange.getEnd().map(date -> "'" + date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "'^^xsd:dateTime");
      if(start.isPresent() || end.isPresent()) {
        builder.countWhere.addWhere(datesWhere);
        if (start.isPresent() && end.isPresent()) {
          builder.dataWhere.addFilter(ef.ge(VAR_DATE, start.get()));
          builder.dataWhere.addFilter(ef.le(VAR_DATE, end.get()));
          builder.countWhere.addFilter(ef.ge(VAR_DATE, start.get()));
          builder.countWhere.addFilter(ef.le(VAR_DATE, end.get()));
        } else if (start.isPresent() && dateRange.isRange()) {
          builder.dataWhere.addFilter(ef.ge(VAR_DATE, start.get()));
          builder.countWhere.addFilter(ef.ge(VAR_DATE, start.get()));
        } else if (start.isPresent()) {
          builder.dataWhere.addFilter(ef.sameTerm(VAR_DATE, start.get()));
          builder.countWhere.addFilter(ef.sameTerm(VAR_DATE, start.get()));
        } else {
          builder.dataWhere.addFilter(ef.le(VAR_DATE, end.get()));
          builder.countWhere.addFilter(ef.le(VAR_DATE, end.get()));
        }
      }
    }

    // Place data
    builder.dataWhere.addWhere(placeWhere);
    if(place.isPresent()) {
      Expr sameTerm = ef.sameTerm(VAR_PLACE, ResourceFactory.createResource(place.get()));
      builder.dataWhere
        .addFilter(sameTerm);
      builder.countWhere
        .addWhere(placeWhere)
        .addFilter(sameTerm);
    }

    // Aspect data
    builder.dataWhere.addWhere(aspectWhere);
    if(aspect.isPresent()){
      Expr sameTerm = ef.sameTerm(VAR_ASPECT, ResourceFactory.createResource(aspect.get()));
      builder.dataWhere
        .addFilter(sameTerm);
      builder.countWhere
        .addWhere(aspectWhere)
        .addFilter(sameTerm);
    }
    if(aspectType.isPresent()) {
      Node varAspect = NodeFactory.createVariable("filterAspectTypeAspect");
      Property type = ResourceFactory.createProperty(aspectType.get());
      builder.dataWhere
        .addWhere(varMain, Core.usesAspect, varAspect)
        .addWhere(varAspect, RDF.type, type);
      builder.countWhere
        .addWhere(varMain, Core.usesAspect, varAspect)
        .addWhere(varAspect, RDF.type, type);
    }
    if(aspectUseType.isPresent()) {
      Node varType = NodeFactory.createVariable("filterAspectUseType");
      Property type = ResourceFactory.createProperty(aspectUseType.get());
      builder.dataWhere
        .addWhere(varMain, type, varType);
      builder.countWhere
        .addWhere(varMain, type, varType);
    }

    /* Set up construction */

    addData(builder, varMain);

    builder.mapper
      .add("dates", Api.eventDatesVar, dates.orElse(""))
      .add("place", Api.eventPlaceVar, place.orElse(""))
      .add("participant", Api.eventParticipantVar, participant.orElse(""))
      .add("participantType", Api.eventParticipantTypeVar, participantType.orElse(""))
      .add("participationType", Api.eventParticipationTypeVar, participationType.orElse(""))
      .add("aspect", Api.eventAspectVar, aspect.orElse(""))
      .add("aspectType", Api.eventAspectTypeVar, aspectType.orElse(""))
      .add("aspectUseType", Api.eventAspectUseTypeVar, aspectUseType.orElse(""));

    // @formatter:on
    return construct(builder);
  }

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #dates,#aspect, #aspectType, #aspectUseType, #participant, #participantType, #participationType, #place}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> dates,
      Optional<String> aspect, Optional<String> aspectType, Optional<String> aspectUseType,
      Optional<String> participant, Optional<String> participantType,
      Optional<String> participationType, Optional<String> place) {
    Model model = findAll(params, dates, aspect, aspectType, aspectUseType, participant,
        participantType, participationType, place);
    return serialize(model, lang, ResourceFactory.createResource(endpointUri("events")));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder = new HydraSingleBuilder(individualsUri(Core.event, id), Core.event);
    Node varMain = HydraSingleBuilder.VAR_MAIN;
    builder.addWhere(datesWhere(varMain, builder.ef, Optional.empty()));
    builder.addWhere(placeWhere(varMain));
    builder.addWhere(participantWhere(varMain));
    builder.addWhere(aspectWhere(varMain));
    addData(builder, varMain);
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(builder.iri));
  }

  private void addData(ConstructBuilder builder, Node varMain) {
    // @formatter:off
    builder
      // Participant
      .addConstruct(varMain, Core.hasParticipant, VAR_PARTICIPANT)
      .addConstruct(VAR_PARTICIPANT, RDF.type, Core.agent)
      .addConstruct(VAR_PARTICIPANT, RDFS.label, VAR_PARTICIPANT_LABEL)
      // Aspect
      .addConstruct(varMain, Core.usesAspect, VAR_ASPECT)
      .addConstruct(VAR_ASPECT, RDF.type, Core.aspect)
      .addConstruct(VAR_ASPECT, RDFS.label, VAR_ASPECT_LABEL)
      .addConstruct(VAR_ASPECT, Core.hasXsdString, VAR_ASPECT_STRING)
      // Place
      .addConstruct(varMain, Core.takesPlaceAt, VAR_PLACE)
      .addConstruct(VAR_PLACE, RDF.type, Core.place)
      .addConstruct(VAR_PLACE, RDF.type, VAR_PLACE_LABEL)
      // Exact date
      .addConstruct(varMain, Core.takesPlaceOn, VAR_DATE_EXACT)
      .addConstruct(VAR_DATE_EXACT, RDF.type, Core.date)
      .addConstruct(VAR_DATE_EXACT, Core.hasXsdDateTime, VAR_DATE_TIME_EXACT)
      // Earliest date
      .addConstruct(varMain, Core.takesPlaceNotEarlierThan, VAR_DATE_EARLIEST)
      .addConstruct(VAR_DATE_EARLIEST, RDF.type, Core.date)
      .addConstruct(VAR_DATE_EARLIEST, Core.hasXsdDateTime, VAR_DATE_TIME_EARLIEST)
      // Latest date
      .addConstruct(varMain, Core.takesPlaceNotLaterThan, VAR_DATE_LATEST)
      .addConstruct(VAR_DATE_LATEST, RDF.type, Core.date)
      .addConstruct(VAR_DATE_LATEST, Core.hasXsdDateTime, VAR_DATE_TIME_LATEST)
      // Sort date
      .addConstruct(varMain, Core.hasSortingDate, VAR_DATE_REAL_SORT)
      .addConstruct(VAR_DATE_REAL_SORT, RDF.type, Core.date)
      .addConstruct(VAR_DATE_REAL_SORT, Core.hasXsdDateTime, VAR_DATE);
    // @formatter:on
  }

  private WhereBuilder aspectWhere(Node varMain) {
    return new WhereBuilder()
        .addOptional(new WhereBuilder().addWhere(varMain, Core.usesAspect, VAR_ASPECT)
            .addWhere(VAR_ASPECT, RDFS.label, VAR_ASPECT_LABEL)
            .addOptional(VAR_ASPECT, Core.hasXsdString, VAR_ASPECT_STRING));
  }

  WhereBuilder datesWhere(Node varMain, ExprFactory ef, Optional<QueryParameters> params) {
    // @formatter:off
    WhereBuilder where =  new WhereBuilder()
      .addOptional(new WhereBuilder()
        .addWhere(varMain, Core.takesPlaceOn, VAR_DATE_EXACT)
        .addWhere(VAR_DATE_EXACT, Core.hasXsdDateTime, VAR_DATE_TIME_EXACT))
      .addOptional(new WhereBuilder()
        .addWhere(varMain, Core.takesPlaceNotEarlierThan, VAR_DATE_EARLIEST)
        .addWhere(VAR_DATE_EARLIEST, Core.hasXsdDateTime, VAR_DATE_TIME_EARLIEST))
      .addOptional(new WhereBuilder()
        .addWhere(varMain, Core.takesPlaceNotLaterThan, VAR_DATE_LATEST)
        .addWhere(VAR_DATE_LATEST, Core.hasXsdDateTime, VAR_DATE_TIME_LATEST))
      .addOptional(new WhereBuilder()
        .addWhere(varMain, Core.hasSortingDate, VAR_DATE_SORT)
        .addWhere(VAR_DATE_SORT, Core.hasXsdDateTime, VAR_DATE_TIME_SORT));
      if(params.isPresent()) { 
        where.addBind(
          ef.cond(ef.bound(VAR_DATE_SORT), ef.asVar(VAR_DATE_SORT), 
            ef.cond(ef.bound(VAR_DATE_EXACT), ef.asVar(VAR_DATE_EXACT), params.get().getOrderByClauses().getOrderFor("date").map(o -> o.equals(Order.ASCENDING)
                ? ef.asExpr(ef.cond(ef.bound(VAR_DATE_LATEST), ef.asVar(VAR_DATE_LATEST), 
                    ef.cond(ef.bound(VAR_DATE_EARLIEST), ef.asVar(VAR_DATE_EARLIEST), 
                        ef.bnode())))
                : ef.asExpr(ef.cond(ef.bound(VAR_DATE_EARLIEST), ef.asVar(VAR_DATE_EARLIEST), 
                    ef.cond(ef.bound(VAR_DATE_LATEST), ef.asVar(VAR_DATE_LATEST), 
                        ef.bnode())))
              ).orElse(ef.asExpr(ef.cond(ef.bound(VAR_DATE_LATEST), ef.asVar(VAR_DATE_LATEST), 
                  ef.cond(ef.bound(VAR_DATE_EARLIEST), ef.asVar(VAR_DATE_EARLIEST), 
                      ef.bnode())))))),
          VAR_DATE_REAL_SORT)
        .addBind(
          ef.cond(ef.bound(VAR_DATE_TIME_SORT), ef.asVar(VAR_DATE_TIME_SORT), 
            ef.cond(ef.bound(VAR_DATE_TIME_EXACT), ef.asVar(VAR_DATE_TIME_EXACT), params.get().getOrderByClauses().getOrderFor("date").map(o -> o.equals(Order.ASCENDING)
                ? ef.asExpr(ef.cond(ef.bound(VAR_DATE_TIME_LATEST), ef.asVar(VAR_DATE_TIME_LATEST), 
                    ef.cond(ef.bound(VAR_DATE_TIME_EARLIEST), ef.asVar(VAR_DATE_TIME_EARLIEST), 
                        ef.asExpr("'9999-12-31T23:59:59'^^xsd:dateTime"))))
                : ef.asExpr(ef.cond(ef.bound(VAR_DATE_TIME_EARLIEST), ef.asVar(VAR_DATE_TIME_EARLIEST), 
                    ef.cond(ef.bound(VAR_DATE_TIME_LATEST), ef.asVar(VAR_DATE_TIME_LATEST), 
                        ef.asExpr("'-9999-01-01T00:00:00'^^xsd:dateTime"))))
              ).orElse(ef.asExpr(ef.cond(ef.bound(VAR_DATE_TIME_LATEST), ef.asVar(VAR_DATE_TIME_LATEST), 
                  ef.cond(ef.bound(VAR_DATE_TIME_EARLIEST), ef.asVar(VAR_DATE_TIME_EARLIEST), 
                      ef.asExpr("'9999-12-31T23:59:59'^^xsd:dateTime"))))))),
          VAR_DATE);
    }
    return where;
    // @formatter:on
  }

  private WhereBuilder participantWhere(Node varMain) {
    return new WhereBuilder().addWhere(varMain, Core.hasParticipant, VAR_PARTICIPANT)
        .addWhere(VAR_PARTICIPANT, RDFS.label, VAR_PARTICIPANT_LABEL);
  }

  private WhereBuilder placeWhere(Node varMain) {
    return new WhereBuilder()
        .addOptional(new WhereBuilder().addWhere(varMain, Core.takesPlaceAt, VAR_PLACE)
            .addWhere(VAR_PLACE, RDFS.label, VAR_PLACE_LABEL));
  }
}
