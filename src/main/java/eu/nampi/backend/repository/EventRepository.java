package eu.nampi.backend.repository;

import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_MAIN;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_TEXT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_TYPE;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.converter.StringToDateRangeConverter;
import eu.nampi.backend.model.DateRange;
import eu.nampi.backend.model.InsertResult;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.ResourceCouple;
import eu.nampi.backend.queryBuilder.HydraBuilderFactory;
import eu.nampi.backend.queryBuilder.HydraCollectionBuilder;
import eu.nampi.backend.queryBuilder.HydraDeleteBuilder;
import eu.nampi.backend.queryBuilder.HydraInsertBuilder;
import eu.nampi.backend.queryBuilder.HydraSingleBuilder;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.util.UrlBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "events")
public class EventRepository {

  @Autowired
  HydraBuilderFactory hydraBuilderFactory;

  @Autowired
  ActRepository actRepository;

  @Autowired
  JenaService jenaService;

  @Autowired
  UrlBuilder urlBuilder;

  private static final String NEGATIVE_DEFAULT_DATE = "-999999-01-01T00:00:00";
  private static final String POSITIVE_DEFAULT_DATE = "999999-01-01T00:00:00";

  private static final StringToDateRangeConverter CONVERTER = new StringToDateRangeConverter();

  private static final String ENDPOINT_NAME = "events";
  private static final Node VAR_ACT = NodeFactory.createVariable("act");
  private static final Node VAR_ACT_DATE = NodeFactory.createVariable("actDate");
  private static final Node VAR_ACT_DATE_TIME = NodeFactory.createVariable("actDateTime");
  private static final Node VAR_ASPECT = NodeFactory.createVariable("aspect");
  private static final Node VAR_ASPECT_LABEL = NodeFactory.createVariable("aspectLabel");
  private static final Node VAR_ASPECT_STRING = NodeFactory.createVariable("aspectString");
  private static final Node VAR_ASPECT_TYPE = NodeFactory.createVariable("aspectType");
  private static final Node VAR_ASPECT_USE_TYPE = NodeFactory.createVariable("aspectUseType");
  private static final Node VAR_AUTHOR = NodeFactory.createVariable("author");
  private static final Node VAR_AUTHOR_LABEL = NodeFactory.createVariable("authorLabel");
  private static final Node VAR_DATE = NodeFactory.createVariable("date");
  private static final Node VAR_DATE_EARLIEST = NodeFactory.createVariable("dateEarliest");
  private static final Node VAR_DATE_EXACT = NodeFactory.createVariable("dateExact");
  private static final Node VAR_DATE_LATEST = NodeFactory.createVariable("dateLatest");
  private static final Node VAR_DATE_OUTER = NodeFactory.createVariable("dateOuter");
  private static final Node VAR_DATE_REAL_SORT = NodeFactory.createVariable("dateRealSort");
  private static final Node VAR_DATE_REAL_SORT_OUTER =
      NodeFactory.createVariable("dateRealSortOuter");
  private static final Node VAR_DATE_SORT = NodeFactory.createVariable("dateSort");
  private static final Node VAR_DATE_TIME_EARLIEST = NodeFactory.createVariable("dateTimeEarliest");
  private static final Node VAR_DATE_TIME_EXACT = NodeFactory.createVariable("dateTimeExact");
  private static final Node VAR_DATE_TIME_LATEST = NodeFactory.createVariable("dateTimeLatest");
  private static final Node VAR_DATE_TIME_SORT = NodeFactory.createVariable("dateTimeSort");
  private static final Node VAR_LOCATION = NodeFactory.createVariable("location");
  private static final Node VAR_LOCATION_TEXT = NodeFactory.createVariable("locationText");
  private static final Node VAR_LOCATION_TEXT_TYPE = NodeFactory.createVariable("locationTextType");
  private static final Node VAR_LOCATION_TYPE = NodeFactory.createVariable("locationType");
  private static final Node VAR_MAIN_PARTICIPANT = NodeFactory.createVariable("mainParticipant");
  private static final Node VAR_MAIN_PARTICIPANT_LABEL =
      NodeFactory.createVariable("mainParticipantLabel");
  private static final Node VAR_PARTICIPANT = NodeFactory.createVariable("participant");
  private static final Node VAR_PARTICIPANT_LABEL = NodeFactory.createVariable("participantLabel");
  private static final Node VAR_PARTICIPANT_TYPE = NodeFactory.createVariable("participantType");
  private static final Node VAR_PARTICIPATION_TYPE =
      NodeFactory.createVariable("participationType");
  private static final Node VAR_PLACE = NodeFactory.createVariable("place");
  private static final Node VAR_PLACE_LABEL = NodeFactory.createVariable("placeLabel");
  private static final Node VAR_PLACE_LATITUDE = NodeFactory.createVariable("placeLatitude");
  private static final Node VAR_PLACE_LONGITUDE = NodeFactory.createVariable("placeLongitude");
  private static final Node VAR_PLACE_SAME_AS = NodeFactory.createVariable("placeSameAs");
  private static final Node VAR_PLACE_TYPE = NodeFactory.createVariable("placeType");
  private static final Node VAR_SOURCE = NodeFactory.createVariable("source");
  private static final Node VAR_SOURCE_LABEL = NodeFactory.createVariable("sourceLocation");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    Optional
        .ofNullable(row.getResource(VAR_TYPE.toString()))
        .ifPresentOrElse(type -> model.add(main, RDF.type, type),
            () -> model.add(main, RDF.type, Core.event));
    // Label
    Optional
        .ofNullable(row.getLiteral(VAR_LABEL.toString()))
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Text
    Optional
        .ofNullable(row.getLiteral(VAR_TEXT.toString()))
        .ifPresent(text -> model.add(main, Core.hasText, text));
    // Comment
    Optional
        .ofNullable(row.getLiteral(VAR_COMMENT.toString()))
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // Act
    Resource act = row.getResource(VAR_ACT.toString());
    Resource author = row.getResource(VAR_AUTHOR.toString());
    Resource date = row.getResource(VAR_ACT_DATE.toString());
    Resource location = row.getResource(VAR_LOCATION.toString());
    Resource source = row.getResource(VAR_SOURCE.toString());
    model
        .add(main, Core.isInterpretationOf, act)
        .add(act, RDF.type, Core.act)
        .add(act, Core.isAuthoredBy, author)
        .add(author, RDF.type, Core.author)
        .add(author, RDFS.label, row.getLiteral(VAR_AUTHOR_LABEL.toString()))
        .add(act, Core.isAuthoredOn, date)
        .add(date, RDF.type, Core.date)
        .add(date, Core.hasDateTime, row.getLiteral(VAR_ACT_DATE_TIME.toString()))
        .add(act, Core.hasSourceLocation, location)
        .add(location, Core.hasSource, source)
        .add(source, RDF.type, Core.source)
        .add(source, RDFS.label, row.getLiteral(VAR_SOURCE_LABEL.toString()));
    Optional
        .ofNullable(row.getResource(VAR_LOCATION_TYPE.toString()))
        .ifPresentOrElse(type -> model.add(location, RDF.type, type),
            () -> model.add(location, RDF.type, Core.sourceLocation));
    Optional.ofNullable(row.getLiteral(VAR_LOCATION_TEXT.toString()))
        .ifPresent(locationText -> {
          model.add(location, Core.hasText, locationText);
          Optional
              .ofNullable(row.getResource(VAR_LOCATION_TEXT_TYPE.toString()))
              .map(type -> ResourceFactory.createProperty(type.getURI()))
              .ifPresent(type -> model.add(location, type, locationText));
        });
    // Aspect
    Optional
        .ofNullable(row.getResource(VAR_ASPECT.toString()))
        .ifPresent(aspect -> {
          Optional
              .ofNullable(row.getResource(VAR_ASPECT_USE_TYPE.toString()))
              .map(type -> ResourceFactory.createProperty(type.getURI()))
              .ifPresentOrElse(type -> model.add(main, type, aspect),
                  () -> model.add(main, Core.usesAspect, aspect));
          Optional
              .ofNullable(row.getLiteral(VAR_ASPECT_LABEL.toString()))
              .ifPresent(label -> model.add(aspect, RDFS.label, label));
          Optional
              .ofNullable(row.getLiteral(VAR_ASPECT_STRING.toString()))
              .ifPresent(str -> model.add(aspect, Core.hasText, str));
          Optional
              .ofNullable(row.getResource(VAR_ASPECT_TYPE.toString()))
              .ifPresentOrElse(type -> model.add(aspect, RDF.type, type),
                  () -> model.add(aspect, RDF.type, Core.aspect));
        });
    // Participant
    Optional
        .ofNullable(row.getResource(VAR_PARTICIPANT.toString()))
        .ifPresent(agent -> {
          model
              .add(main, Core.hasParticipant, agent)
              .add(agent, RDFS.label, row.getLiteral(VAR_PARTICIPANT_LABEL.toString()));
          Optional
              .ofNullable(row.getResource(VAR_PARTICIPATION_TYPE.toString()))
              .map(type -> ResourceFactory.createProperty(type.getURI()))
              .ifPresentOrElse(type -> model.add(main, type, agent),
                  () -> model.add(main, Core.hasParticipant, agent));
          Optional
              .ofNullable(row.getResource(VAR_PARTICIPANT_TYPE.toString()))
              .ifPresentOrElse(type -> model.add(agent, RDF.type, type),
                  () -> model.add(agent, RDF.type, Core.actor));
        });
    // Main participant
    Optional
        .ofNullable(row.getResource(VAR_MAIN_PARTICIPANT.toString()))
        .ifPresent(participant -> model
            .add(main, Core.hasMainParticipant, participant)
            .add(participant, RDFS.label, row.getLiteral(VAR_MAIN_PARTICIPANT_LABEL.toString())));
    // Place
    Optional
        .ofNullable(row.getResource(VAR_PLACE.toString()))
        .ifPresent(place -> {
          model
              .add(main, Core.takesPlaceAt, place)
              .add(place, RDFS.label, row.getLiteral(VAR_PLACE_LABEL.toString()));
          Optional.ofNullable(row.getResource(VAR_PLACE_SAME_AS.toString()))
              .ifPresent(sameAs -> model.add(place, Core.sameAs, sameAs));
          Optional
              .ofNullable(row.getResource(VAR_PLACE_TYPE.toString()))
              .ifPresentOrElse(type -> model.add(place, RDF.type, type),
                  () -> model.add(place, RDF.type, Core.place));
          Optional.ofNullable(row.getLiteral(VAR_PLACE_LATITUDE.toString())).ifPresent(
              latitude -> Optional.ofNullable(row.getLiteral(VAR_PLACE_LONGITUDE.toString()))
                  .ifPresent(longitude -> model
                      .add(place, Core.hasLatitude, latitude)
                      .add(place, Core.hasLongitude, longitude)));
        });
    // Exact date
    Optional
        .ofNullable(row.getLiteral(VAR_DATE_TIME_EXACT.toString()))
        .ifPresent(dateTime -> {
          Resource resDate = row.getResource(VAR_DATE_EXACT.toString());
          model
              .add(main, Core.takesPlaceOn, resDate)
              .add(resDate, RDF.type, Core.date)
              .add(resDate, Core.hasDateTime, dateTime);
        });
    // Earliest date
    Optional
        .ofNullable(row.getLiteral(VAR_DATE_TIME_EARLIEST.toString()))
        .ifPresent(dateTime -> {
          Resource resDate = row.getResource(VAR_DATE_EARLIEST.toString());
          model
              .add(main, Core.takesPlaceNotEarlierThan, resDate)
              .add(resDate, RDF.type, Core.date)
              .add(resDate, Core.hasDateTime, dateTime);
        });
    // Latest date
    Optional
        .ofNullable(row.getLiteral(VAR_DATE_TIME_LATEST.toString()))
        .ifPresent(dateTime -> {
          Resource resDate = row.getResource(VAR_DATE_LATEST.toString());
          model
              .add(main, Core.takesPlaceNotLaterThan, resDate)
              .add(resDate, RDF.type, Core.date)
              .add(resDate, Core.hasDateTime, dateTime);
        });
    // Sorting date
    Optional
        .ofNullable(row.getLiteral(VAR_DATE_OUTER.toString()))
        .ifPresent(dateTime -> {
          Resource resDate = row.getResource(VAR_DATE_REAL_SORT_OUTER.toString());
          model
              .add(main, Core.hasSortingDate, resDate)
              .add(resDate, RDF.type, Core.date)
              .add(resDate, Core.hasDateTime, dateTime);
        });
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #dates, #aspect, #aspectType, #aspectUseType, #participant, #participantType, #participationType, #place, #author, #source}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> dates,
      Optional<Resource> aspect, Optional<Resource> aspectType, Optional<Property> aspectUseType,
      Optional<Resource> participant, Optional<Resource> participantType,
      Optional<Property> participationType, Optional<Resource> place, Optional<Resource> author,
      Optional<Resource> source) {
    HydraCollectionBuilder builder = hydraBuilderFactory.collectionBuilder(ENDPOINT_NAME,
        Core.event, Api.eventOrderByProp, params, false);
    ExprFactory ef = builder.ef;
    boolean hasDateSort = params.getOrderByClauses().containsKey("date");
    Order order = params.getOrderByClauses().getOrderFor("date").orElse(Order.ASCENDING);
    // Add custom text select
    params.getText().ifPresent(text -> {
      Node varSearchString = NodeFactory.createVariable("searchString");
      Path path = PathFactory.pathAlt(PathFactory.pathLink(RDFS.label.asNode()),
          PathFactory.pathLink(Core.hasText.asNode()));
      builder.coreData.addOptional(VAR_MAIN, path, varSearchString)
          .addFilter(ef.regex(varSearchString, params.getText().get(), "i"));
    });
    // Place data
    builder.mapper.add("place", Api.eventPlaceProp, place);
    builder.extendedData
        .addOptional(VAR_MAIN, Core.hasText, VAR_TEXT);
    place.ifPresent(resPlace -> builder.coreData.addWhere(placeWhere(false))
        .addFilter(ef.sameTerm(VAR_PLACE, resPlace)));
    // Participant data
    AtomicBoolean useParticipantWhere = new AtomicBoolean(false);
    builder.mapper.add("participant", Api.eventParticipantProp, participant);
    participant.ifPresent(resParticipant -> {
      if (!useParticipantWhere.get()) {
        builder.coreData.addWhere(participantWhere(false));
      }
      builder.coreData.addFilter(ef.sameTerm(VAR_PARTICIPANT, resParticipant));
      useParticipantWhere.set(true);
    });
    // Participant type data
    builder.mapper.add("participantType", Api.eventParticipantTypeProp, participantType);
    participantType.ifPresent(resType -> {
      if (!useParticipantWhere.get()) {
        builder.coreData.addWhere(participantWhere(false));
      }
      builder.coreData.addWhere(VAR_PARTICIPANT, RDF.type, resType);
      useParticipantWhere.set(true);
    });
    // Participation type data
    builder.mapper.add("participationType", Api.eventParticipationTypeProp, participationType);
    participationType
        .ifPresent(resType -> builder.coreData.addWhere(VAR_MAIN, resType, VAR_PARTICIPANT));

    AtomicBoolean useAspectWhere = new AtomicBoolean(false);
    // Aspect data
    builder.mapper.add("aspect", Api.eventAspectProp, aspect);
    aspect.ifPresent(resAspect -> {
      if (!useAspectWhere.get()) {
        builder.coreData.addWhere(aspectWhere(false));
      }
      builder.coreData.addFilter(ef.sameTerm(VAR_ASPECT, resAspect));
      useAspectWhere.set(true);
    });
    // Aspect type data
    builder.mapper.add("aspectType", Api.eventAspectTypeProp, aspectType);
    aspectType.ifPresent(resType -> {
      if (!useAspectWhere.get()) {
        builder.coreData.addWhere(aspectWhere(false));
      }
      builder.coreData.addWhere(VAR_ASPECT, RDF.type, resType);
      useAspectWhere.set(true);
    });
    // Aspect use type data
    builder.mapper.add("aspectUseType", Api.eventAspectUseTypeProp, aspectUseType);
    aspectUseType.ifPresent(resType -> builder.coreData.addWhere(VAR_MAIN, resType, VAR_ASPECT));
    // Author data
    builder.mapper.add("author", Api.eventAuthorProp, author);
    author.ifPresent(resAuthor -> builder.coreData.addWhere(actWhere(false))
        .addFilter(ef.sameTerm(VAR_AUTHOR, resAuthor)));
    // Source data
    builder.mapper.add("source", Api.eventSourceProp, source);
    source.ifPresent(resSource -> builder.coreData.addWhere(actWhere(false))
        .addFilter(ef.sameTerm(VAR_SOURCE, resSource)));
    // Dates data
    if (hasDateSort || dates.isPresent()) {
      builder.coreData.addWhere(datesWhere(order, VAR_DATE_REAL_SORT, VAR_DATE));
    }
    builder.mapper.add("dates", Api.eventDatesProp, dates);
    dates.ifPresent(datesString -> {
      DateRange dateRange = CONVERTER.convert(datesString);
      Optional<Expr> start =
          dateRange.getStart().map(date -> ef.asExpr(ResourceFactory.createTypedLiteral(
              date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), XSDDateType.XSDdateTime)));
      Optional<Expr> end =
          dateRange.getEnd().map(date -> ef.asExpr(ResourceFactory.createTypedLiteral(
              date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), XSDDateType.XSDdateTime)));
      if (start.isPresent() || end.isPresent()) {
        if (start.isPresent() && end.isPresent()) {
          builder.coreData.addFilter(ef.ge(VAR_DATE, start.get()))
              .addFilter(ef.le(VAR_DATE, end.get()));
        } else if (start.isPresent() && dateRange.isRange()) {
          builder.coreData.addFilter(ef.ge(VAR_DATE, start.get()));
        } else if (start.isPresent()) {
          builder.coreData.addFilter(ef.sameTerm(VAR_DATE, start.get()));
        } else {
          builder.coreData.addFilter(ef.le(VAR_DATE, end.get()));
        }
      }
    });
    builder.extendedData
        .addWhere(datesWhere(order, VAR_DATE_REAL_SORT_OUTER, VAR_DATE_OUTER))
        .addWhere(actWhere(false))
        .addWhere(participantWhere(false)).addOptional(aspectWhere(false))
        .addOptional(placeWhere(false));
    return builder.query(ROW_MAPPER, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder = hydraBuilderFactory.singleBuilder(ENDPOINT_NAME, id, Core.event);
    builder.coreData
        .addOptional(VAR_MAIN, Core.hasText, VAR_TEXT)
        .addWhere(datesWhere(Order.ASCENDING, VAR_DATE_REAL_SORT, VAR_DATE))
        .addWhere(actWhere(true))
        .addWhere(participantWhere(true))
        .addOptional(aspectWhere(true))
        .addOptional(placeWhere(true));
    return builder.query(ROW_MAPPER, lang);
  }

  private WhereBuilder actWhere(boolean withTypes) {
    WhereBuilder builder = new WhereBuilder();
    ExprFactory ef = builder.getExprFactory();
    WhereBuilder locationBuilder = new WhereBuilder()
        .addWhere(VAR_LOCATION, Core.hasText, VAR_LOCATION_TEXT);
    if (withTypes) {
      locationBuilder
          .addWhere(VAR_LOCATION, VAR_LOCATION_TEXT_TYPE, VAR_LOCATION_TEXT)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TEXT), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TEXT), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TEXT), RDF.getURI())));
    }
    builder
        .addWhere(VAR_MAIN, Core.isInterpretationOf, VAR_ACT)
        .addWhere(VAR_ACT, Core.isAuthoredBy, VAR_AUTHOR)
        .addWhere(VAR_AUTHOR, RDFS.label, VAR_AUTHOR_LABEL)
        .addWhere(VAR_ACT, Core.isAuthoredOn, VAR_ACT_DATE)
        .addWhere(VAR_ACT_DATE, Core.hasDateTime, VAR_ACT_DATE_TIME)
        .addWhere(VAR_ACT, Core.hasSourceLocation, VAR_LOCATION)
        .addWhere(VAR_LOCATION, Core.hasSource, VAR_SOURCE)
        .addWhere(VAR_SOURCE, RDFS.label, VAR_SOURCE_LABEL)
        .addOptional(locationBuilder);
    if (withTypes) {
      builder
          .addWhere(VAR_LOCATION, RDF.type, VAR_LOCATION_TYPE)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TYPE), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TYPE), RDF.getURI())));
    }
    return builder;
  }

  private WhereBuilder aspectWhere(boolean withTypes) {
    WhereBuilder builder = new WhereBuilder();
    ExprFactory ef = builder.getExprFactory();
    builder
        .addWhere(VAR_MAIN, Core.usesAspect, VAR_ASPECT)
        .addWhere(VAR_ASPECT, RDFS.label, VAR_ASPECT_LABEL)
        .addOptional(VAR_ASPECT, Core.hasText, VAR_ASPECT_STRING);
    if (withTypes) {
      builder
          .addWhere(VAR_ASPECT, RDF.type, VAR_ASPECT_TYPE)
          .addWhere(VAR_MAIN, VAR_ASPECT_USE_TYPE, VAR_ASPECT)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_ASPECT_USE_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_ASPECT_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_ASPECT_TYPE), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_ASPECT_TYPE), RDF.getURI())));
    }
    return builder;
  }

  private WhereBuilder datesWhere(Order order, Node varDate, Node varDateTime) {
    WhereBuilder builder = new WhereBuilder();
    ExprFactory ef = builder.getExprFactory();
    builder
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.takesPlaceOn, VAR_DATE_EXACT)
            .addWhere(VAR_DATE_EXACT, Core.hasDateTime, VAR_DATE_TIME_EXACT))
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.takesPlaceNotEarlierThan, VAR_DATE_EARLIEST)
            .addWhere(VAR_DATE_EARLIEST, Core.hasDateTime, VAR_DATE_TIME_EARLIEST))
        .addOptional(
            new WhereBuilder()
                .addWhere(VAR_MAIN, Core.takesPlaceNotLaterThan, VAR_DATE_LATEST)
                .addWhere(VAR_DATE_LATEST, Core.hasDateTime, VAR_DATE_TIME_LATEST))
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.hasSortingDate, VAR_DATE_SORT)
            .addWhere(VAR_DATE_SORT, Core.hasDateTime, VAR_DATE_TIME_SORT))
        .addBind(ef.cond(ef.bound(VAR_DATE_SORT), ef.asVar(VAR_DATE_SORT),
            ef.cond(ef.bound(VAR_DATE_EXACT), ef.asVar(VAR_DATE_EXACT),
                order.equals(Order.ASCENDING)
                    ? ef.asExpr(
                        ef.cond(ef.bound(VAR_DATE_LATEST), ef.asVar(VAR_DATE_LATEST),
                            ef.cond(ef.bound(VAR_DATE_EARLIEST), ef.asVar(VAR_DATE_EARLIEST),
                                ef.bnode())))
                    : ef.asExpr(ef.cond(ef.bound(VAR_DATE_EARLIEST), ef.asVar(VAR_DATE_EARLIEST),
                        ef.cond(ef.bound(VAR_DATE_LATEST), ef.asVar(VAR_DATE_LATEST),
                            ef.bnode()))))),
            varDate)
        .addBind(
            ef.cond(
                ef.bound(VAR_DATE_TIME_SORT), ef.asVar(VAR_DATE_TIME_SORT), ef
                    .cond(ef.bound(VAR_DATE_TIME_EXACT), ef.asVar(VAR_DATE_TIME_EXACT),
                        order.equals(Order.ASCENDING)
                            ? ef.asExpr(
                                ef.cond(ef.bound(VAR_DATE_TIME_LATEST),
                                    ef.asVar(VAR_DATE_TIME_LATEST),
                                    ef.cond(ef.bound(VAR_DATE_TIME_EARLIEST),
                                        ef.asVar(VAR_DATE_TIME_EARLIEST),
                                        ef.asExpr(ResourceFactory.createTypedLiteral(
                                            POSITIVE_DEFAULT_DATE, XSDDatatype.XSDdateTime)))))
                            : ef.asExpr(ef.cond(ef.bound(VAR_DATE_TIME_EARLIEST),
                                ef.asVar(VAR_DATE_TIME_EARLIEST),
                                ef.cond(ef.bound(VAR_DATE_TIME_LATEST),
                                    ef.asVar(VAR_DATE_TIME_LATEST),
                                    ef.asExpr(ResourceFactory.createTypedLiteral(
                                        NEGATIVE_DEFAULT_DATE, XSDDatatype.XSDdateTime))))))),
            varDateTime);
    return builder;
  }

  private WhereBuilder participantWhere(boolean withTypes) {
    WhereBuilder builder = new WhereBuilder();
    ExprFactory ef = builder.getExprFactory();
    builder
        .addWhere(VAR_MAIN, Core.hasParticipant, VAR_PARTICIPANT)
        .addWhere(VAR_PARTICIPANT, RDFS.label, VAR_PARTICIPANT_LABEL)
        .addWhere(VAR_MAIN, Core.hasMainParticipant, VAR_MAIN_PARTICIPANT)
        .addWhere(VAR_MAIN_PARTICIPANT, RDFS.label, VAR_MAIN_PARTICIPANT_LABEL);
    if (withTypes) {
      builder
          .addWhere(VAR_MAIN, VAR_PARTICIPATION_TYPE, VAR_PARTICIPANT)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PARTICIPATION_TYPE), OWL.getURI())))
          .addWhere(VAR_PARTICIPANT, RDF.type, VAR_PARTICIPANT_TYPE)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PARTICIPANT_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PARTICIPANT_TYPE), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PARTICIPANT_TYPE), RDF.getURI())));
    }
    return builder;
  }

  private WhereBuilder placeWhere(boolean withTypes) {
    WhereBuilder builder = new WhereBuilder();
    ExprFactory ef = builder.getExprFactory();
    builder
        .addWhere(VAR_MAIN, Core.takesPlaceAt, VAR_PLACE)
        .addWhere(VAR_PLACE, RDFS.label, VAR_PLACE_LABEL)
        .addOptional(VAR_PLACE, Core.sameAs, VAR_PLACE_SAME_AS)
        .addOptional(VAR_PLACE, Core.hasLatitude, VAR_PLACE_LATITUDE)
        .addOptional(VAR_PLACE, Core.hasLongitude, VAR_PLACE_LONGITUDE);
    if (withTypes) {
      builder
          .addWhere(VAR_PLACE, RDF.type, VAR_PLACE_TYPE)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PLACE_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PLACE_TYPE), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PLACE_TYPE), RDF.getURI())));
    }
    return builder;
  }

  public void delete(UUID id) {
    HydraDeleteBuilder builder = hydraBuilderFactory.deleteBuilder(id, ENDPOINT_NAME, Core.event);
    // Delete event
    ExprFactory ef = builder.updateBuilder.getExprFactory();
    Node varHasDatePredicate = NodeFactory.createVariable("hasDatePredicate");
    Node varDate = NodeFactory.createVariable("date");
    Node varDatePredicate = NodeFactory.createVariable("datePredicate");
    Node varDateObject = NodeFactory.createVariable("dateObject");
    builder
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, varHasDatePredicate, varDate)
            .addWhere(varDate, varDatePredicate, varDateObject)
            .addFilter(ef.in(
                varHasDatePredicate,
                Core.hasDate, Core.takesPlaceNotEarlierThan,
                Core.takesPlaceNotLaterThan, Core.takesPlaceOn)))
        .addDelete(VAR_MAIN, varHasDatePredicate, varDate)
        .addDelete(varDate, varDatePredicate, varDateObject);
    // Delete act
    actRepository.findOne(builder.root).ifPresent(act -> actRepository.delete(act));
    builder.build();
  }

  public InsertResult insert(Optional<UUID> optionalId, Lang lang, List<Resource> types,
      List<Literal> labels, List<Literal> comments, List<Literal> texts, List<Resource> authors,
      Resource source, Optional<Literal> sourceLocation, ResourceCouple mainParticipant,
      List<ResourceCouple> otherParticipants, List<ResourceCouple> aspects,
      Optional<Resource> optionalPlace, Optional<DateRange> optionalDate) {
    validatePayload(lang, types, labels, comments, texts, authors, source, sourceLocation,
        mainParticipant, otherParticipants, aspects, optionalPlace, optionalDate);
    // Create builder depending on whether or not id is present
    HydraInsertBuilder builder = optionalId
        .map(id -> hydraBuilderFactory.insertBuilder(lang, id, ENDPOINT_NAME, types,
            labels, comments, texts, new ArrayList<>()))
        .orElse(hydraBuilderFactory.insertBuilder(lang, ENDPOINT_NAME, types,
            labels, comments, texts, new ArrayList<>()));
    // Event main participant
    mainParticipant.getPredicate().ifPresentOrElse(predicate -> {
      builder.addInsert(builder.root, predicate, mainParticipant.getObject());
    }, () -> builder.addInsert(builder.root, Core.hasMainParticipant, mainParticipant.getObject()));
    // Other event participants
    otherParticipants.forEach(participant -> {
      participant.getPredicate().ifPresentOrElse(predicate -> {
        builder.addInsert(builder.root, predicate, participant.getObject());
      }, () -> builder.addInsert(builder.root, Core.hasOtherParticipant, participant.getObject()));
    });
    // Event aspects
    aspects.forEach(aspect -> {
      aspect.getPredicate().ifPresentOrElse(predicate -> {
        builder.addInsert(builder.root, predicate, aspect.getObject());
      }, () -> builder.addInsert(builder.root, Core.usesAspect, aspect.getObject()));
    });
    // Event place
    optionalPlace.ifPresent(place -> {
      builder.addInsert(builder.root, Core.takesPlaceAt, place);
    });
    // Event date
    optionalDate.ifPresent(range -> {
      if (range.getStart().isPresent() || range.getEnd().isPresent()) {
        if (range.isRange()) {
          range.getStart().ifPresent(start -> {
            Resource startRes = ResourceFactory.createResource();
            builder
                .addInsert(builder.root, Core.takesPlaceNotEarlierThan, startRes)
                .addInsert(startRes, RDF.type, Core.date)
                .addInsert(startRes, Core.hasDateTime,
                    ResourceFactory.createTypedLiteral(
                        start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        XSDDatatype.XSDdateTime));
          });
          range.getEnd().ifPresent(end -> {
            Resource endRes = ResourceFactory.createResource();
            builder
                .addInsert(builder.root, Core.takesPlaceNotLaterThan, endRes)
                .addInsert(endRes, RDF.type, Core.date)
                .addInsert(endRes, Core.hasDateTime,
                    ResourceFactory.createTypedLiteral(
                        end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        XSDDatatype.XSDdateTime));
          });
        } else {
          Resource exactRes = ResourceFactory.createResource();
          builder
              .addInsert(builder.root, Core.takesPlaceOn, exactRes)
              .addInsert(exactRes, RDF.type, Core.date)
              .addInsert(exactRes, Core.hasDateTime, ResourceFactory
                  .createTypedLiteral(
                      range.getStart().get().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                      XSDDatatype.XSDdateTime));
        }
      }
    });
    // Connected act
    builder.addInsert(builder.root, Core.isInterpretationOf,
        actRepository.insert(lang, authors, source, sourceLocation));
    builder.build();
    // Insert Document Interpretation Act
    String result = findOne(lang, builder.id);
    return new InsertResult(builder.root, result);
  }

  public InsertResult insert(Lang lang, List<Resource> types, List<Literal> labels,
      List<Literal> comments, List<Literal> texts, List<Resource> authors, Resource source,
      Optional<Literal> sourceLocation, ResourceCouple mainParticipant,
      List<ResourceCouple> otherParticipants, List<ResourceCouple> aspects,
      Optional<Resource> optionalPlace, Optional<DateRange> optionalDate) {
    return insert(Optional.empty(), lang, types, labels, comments, texts, authors, source,
        sourceLocation, mainParticipant, otherParticipants, aspects, optionalPlace, optionalDate);
  }

  public String update(Lang lang, UUID id, List<Resource> types, List<Literal> labels,
      List<Literal> comments, List<Literal> texts, List<Resource> authors, Resource source,
      Optional<Literal> sourceLocation, ResourceCouple mainParticipant,
      List<ResourceCouple> otherParticipants, List<ResourceCouple> aspects,
      Optional<Resource> optionalPlace, Optional<DateRange> optionalDate) {
    validatePayload(lang, types, labels, comments, texts, authors, source, sourceLocation,
        mainParticipant, otherParticipants, aspects, optionalPlace, optionalDate);
    delete(id);
    insert(Optional.of(id), lang, types, labels, comments, texts, authors, source, sourceLocation,
        mainParticipant, otherParticipants, aspects, optionalPlace, optionalDate);
    return findOne(lang, id);
  }

  private void validatePayload(Lang lang, List<Resource> types,
      List<Literal> labels, List<Literal> comments, List<Literal> texts, List<Resource> authors,
      Resource source, Optional<Literal> sourceLocation, ResourceCouple mainParticipant,
      List<ResourceCouple> otherParticipants, List<ResourceCouple> aspects,
      Optional<Resource> optionalPlace, Optional<DateRange> optionalDate) {
    HydraInsertBuilder builder = hydraBuilderFactory.insertBuilder(lang, ENDPOINT_NAME, types,
        labels, comments, texts, new ArrayList<>());
    builder.validateSubresources(Core.event, types);
    // Author
    authors.forEach(author -> builder.validateType(Core.author, author));
    // Source
    builder.validateType(Core.source, source);
    // Date
    optionalDate
        .ifPresent(date -> date.getStart().ifPresent(start -> date.getEnd().ifPresent(end -> {
          if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                String.format("Start date '%s' is not allowed to be after '%s'",
                    start.format(DateTimeFormatter.ISO_DATE),
                    end.format(DateTimeFormatter.ISO_DATE)));
          }
        })));
    // Event main participant
    builder.validateType(Core.person, mainParticipant.getObject());
    mainParticipant.getPredicate().ifPresent(predicate -> {
      builder.validateSubnode(Core.hasMainParticipant, predicate);
    });
    // Other event participants
    otherParticipants.forEach(participant -> {
      if (participant.getObject().getURI().equals(mainParticipant.getObject().getURI())) {
        throw new IllegalArgumentException(
            "The main participant is not allowed to occur in the list of other participants");
      }
      builder.validateType(Core.actor, participant.getObject());
      participant.getPredicate().ifPresent(predicate -> {
        builder.validateNotSubnode(Core.hasMainParticipant, predicate);
        builder.validateSubnode(Core.hasOtherParticipant, predicate);
      });
    });
    // Event aspects
    aspects.forEach(aspect -> {
      builder.validateType(Core.aspect, aspect.getObject());
      aspect.getPredicate().ifPresent(predicate -> {
        builder.validateSubnode(Core.usesAspect, predicate);
      });
    });
    // Event place
    optionalPlace.ifPresent(place -> {
      builder.validateType(Core.place, place);
    });
  }

  @Cacheable(key = "{#authorId.toString(), #eventId.toString()}")
  public boolean isAuthor(UUID authorId, UUID eventId) {
    Resource event = ResourceFactory.createResource(urlBuilder.endpointUri(ENDPOINT_NAME, eventId));
    Resource author = ResourceFactory.createResource(urlBuilder.endpointUri("authors", authorId));
    Path path = PathFactory.pathSeq(PathFactory.pathLink(Core.isInterpretationOf.asNode()),
        PathFactory.pathLink(Core.isAuthoredBy.asNode()));
    AskBuilder builder = new AskBuilder().addWhere(event, path, author);
    return jenaService.ask(builder);
  }
}
