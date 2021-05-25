package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_MAIN;
import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_TYPE;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
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
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.converter.StringToDateRangeConverter;
import eu.nampi.backend.model.DateRange;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.AbstractHydraBuilder;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "events")
public class EventRepository extends AbstractHydraRepository {

  private static final String NEGATIVE_DEFAULT_DATE = "-9999-01-01T00:00:00";
  private static final String POSITIVE_DEFAULT_DATE = "9999-01-01T00:00:00";

  private static final StringToDateRangeConverter CONVERTER = new StringToDateRangeConverter();

  private static final Node VAR_ACT = NodeFactory.createVariable("act");
  private static final Node VAR_ACT_DATE = NodeFactory.createVariable("actDate");
  private static final Node VAR_ACT_DATE_TIME = NodeFactory.createVariable("actDateTime");
  private static final Node VAR_ASPECT = NodeFactory.createVariable("aspect");
  private static final Node VAR_ASPECT_LABEL = NodeFactory.createVariable("aspectLabel");
  private static final Node VAR_ASPECT_STRING = NodeFactory.createVariable("aspectString");
  private static final Node VAR_ASPECT_TYPE = NodeFactory.createVariable("aspectType");
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
  private static final Node VAR_PLACE_TYPE = NodeFactory.createVariable("placeType");
  private static final Node VAR_SOURCE = NodeFactory.createVariable("source");
  private static final Node VAR_SOURCE_LABEL = NodeFactory.createVariable("sourceLocation");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    Optional.ofNullable(row.getResource(VAR_TYPE.toString())).ifPresentOrElse(type -> {
      model.add(main, RDF.type, type);
    }, () -> {
      model.add(main, RDF.type, Core.event);
    });
    // Label
    Optional.ofNullable(row.getLiteral(VAR_LABEL.toString())).map(Literal::getString)
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional.ofNullable(row.getLiteral(VAR_COMMENT.toString())).map(Literal::getString)
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // Act
    Resource act = row.getResource(VAR_ACT.toString());
    Resource author = row.getResource(VAR_AUTHOR.toString());
    Resource date = row.getResource(VAR_ACT_DATE.toString());
    Resource location = row.getResource(VAR_LOCATION.toString());
    Resource source = row.getResource(VAR_SOURCE.toString());
    Literal locationText = row.getLiteral(VAR_LOCATION_TEXT.toString());
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
        .add(location, Core.hasText, locationText)
        .add(location, Core.hasSource, source)
        .add(source, RDF.type, Core.source)
        .add(source, RDFS.label, row.getLiteral(VAR_SOURCE_LABEL.toString()));
    Optional.ofNullable(row.getResource(VAR_LOCATION_TYPE.toString())).ifPresentOrElse(
        type -> model.add(location, RDF.type, type),
        () -> model.add(location, RDF.type, Core.sourceLocation));
    Optional.ofNullable(row.getResource(VAR_LOCATION_TEXT_TYPE.toString()))
        .map(type -> ResourceFactory.createProperty(type.getURI())).ifPresent(
            type -> model.add(location, type, locationText));
    // Aspect
    Optional.ofNullable(row.getResource(VAR_ASPECT.toString()))
        .ifPresent(aspect -> {
          model.add(main, Core.usesAspect, aspect);
          Optional.ofNullable(row.getLiteral(VAR_ASPECT_LABEL.toString()))
              .ifPresent(label -> model.add(aspect, RDFS.label, label));
          Optional.ofNullable(row.getLiteral(VAR_ASPECT_STRING.toString()))
              .ifPresent(str -> model.add(aspect, Core.hasText, str));
          Optional.ofNullable(row.getResource(VAR_ASPECT_TYPE.toString())).ifPresentOrElse(
              type -> model.add(aspect, RDF.type, type),
              () -> model.add(aspect, RDF.type, Core.aspect));
        });
    // Participant
    Optional.ofNullable(row.getResource(VAR_PARTICIPANT.toString()))
        .ifPresent(agent -> {
          model
              .add(main, Core.hasParticipant, agent)
              .add(agent, RDFS.label, row.getLiteral(VAR_PARTICIPANT_LABEL.toString()));
          Optional.ofNullable(row.getResource(VAR_PARTICIPATION_TYPE.toString()))
              .map(type -> ResourceFactory.createProperty(type.getURI())).ifPresentOrElse(
                  type -> model.add(main, type, agent),
                  () -> model.add(main, Core.hasParticipant, agent));
          Optional.ofNullable(row.getResource(VAR_PARTICIPANT_TYPE.toString())).ifPresentOrElse(
              type -> model.add(agent, RDF.type, type),
              () -> model.add(agent, RDF.type, Core.agent));
        });
    // Main participant
    Optional.ofNullable(row.getResource(VAR_MAIN_PARTICIPANT.toString()))
        .ifPresent(participant -> model.add(main, Core.hasMainParticipant, participant)
            .add(participant, RDFS.label, row.getLiteral(VAR_MAIN_PARTICIPANT_LABEL.toString())));
    // Place
    Optional.ofNullable(row.getResource(VAR_PLACE.toString()))
        .ifPresent(place -> {
          model
              .add(main, Core.takesPlaceAt, place)
              .add(place, RDFS.label, row.getLiteral(VAR_PLACE_LABEL.toString()));
          Optional.ofNullable(row.getResource(VAR_PLACE_TYPE.toString())).ifPresentOrElse(
              type -> model.add(place, RDF.type, type),
              () -> model.add(place, RDF.type, Core.place));
        });

    // Exact date
    Optional.ofNullable(row.getLiteral(VAR_DATE_TIME_EXACT.toString())).ifPresent(dateTime -> {
      Resource resDate = row.getResource(VAR_DATE_EXACT
          .toString());
      model
          .add(main, Core.takesPlaceOn, resDate)
          .add(resDate, RDF.type, Core.date)
          .add(resDate, Core.hasDateTime, dateTime);
    });
    // Earliest date
    Optional.ofNullable(row.getLiteral(VAR_DATE_TIME_EARLIEST.toString())).ifPresent(dateTime -> {
      Resource resDate = row.getResource(VAR_DATE_EARLIEST.toString());
      model
          .add(main, Core.takesPlaceNotEarlierThan, resDate)
          .add(resDate, RDF.type, Core.date)
          .add(resDate, Core.hasDateTime, dateTime);
    });
    // Latest date
    Optional.ofNullable(row.getLiteral(VAR_DATE_TIME_LATEST.toString())).ifPresent(dateTime -> {
      Resource resDate = row.getResource(VAR_DATE_LATEST.toString());
      model
          .add(main, Core.takesPlaceNotLaterThan, resDate)
          .add(resDate, RDF.type, Core.date)
          .add(resDate, Core.hasDateTime, dateTime);
    });
    // Sorting date
    Optional.ofNullable(row.getLiteral(VAR_DATE_OUTER.toString())).ifPresent(dateTime -> {
      Resource resDate = row.getResource(VAR_DATE_REAL_SORT_OUTER.toString());
      model
          .add(main, Core.hasSortingDate, resDate)
          .add(resDate, RDF.type, Core.date)
          .add(resDate, Core.hasDateTime, dateTime);
    });
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #dates,#aspect, #aspectType, #aspectUseType, #participant, #participantType, #participationType, #place, #author}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> dates,
      Optional<String> aspect, Optional<String> aspectType, Optional<String> aspectUseType,
      Optional<String> participant, Optional<String> participantType,
      Optional<String> participationType, Optional<String> place, Optional<String> author) {

    HydraCollectionBuilder builder = new HydraCollectionBuilder(jenaService, endpointUri("events"),
        Core.event, Api.eventOrderByVar, params);
    ExprFactory ef = builder.ef;

    boolean hasDateSort = params.getOrderByClauses().containsKey("date");
    Order order = params.getOrderByClauses().getOrderFor("date").orElse(Order.ASCENDING);

    // Place data
    builder.mapper.add("place", Api.eventPlaceVar, place);
    place.map(ResourceFactory::createResource).ifPresent(resPlace -> {
      builder.coreData
          .addWhere(placeWhere(false))
          .addFilter(ef.sameTerm(VAR_PLACE, resPlace));
    });

    AtomicBoolean useParticipantWhere = new AtomicBoolean(false);
    // Participant data
    builder.mapper.add("participant", Api.eventParticipantVar, participant);
    participant.map(ResourceFactory::createResource).ifPresent(resParticipant -> {
      if (!useParticipantWhere.get()) {
        builder.coreData.addWhere(participantWhere(false));
      }
      builder.coreData.addFilter(ef.sameTerm(VAR_PARTICIPANT, resParticipant));
      useParticipantWhere.set(true);
    });
    // Participant type data
    builder.mapper.add("participantType", Api.eventParticipantTypeVar, participantType);
    participantType.map(ResourceFactory::createResource).ifPresent(resType -> {
      if (!useParticipantWhere.get()) {
        builder.coreData.addWhere(participantWhere(false));
      }
      builder.coreData.addWhere(VAR_PARTICIPANT, RDF.type, resType);
      useParticipantWhere.set(true);
    });
    // Participation type data
    builder.mapper.add("participationType", Api.eventParticipationTypeVar, participationType);
    participationType.map(ResourceFactory::createResource).ifPresent(resType -> {
      builder.coreData.addWhere(VAR_MAIN, resType, VAR_PARTICIPANT);
    });

    AtomicBoolean useAspectWhere = new AtomicBoolean(false);
    // Aspect data
    builder.mapper.add("aspect", Api.eventAspectVar, aspect);
    aspect.map(ResourceFactory::createResource).ifPresent(resAspect -> {
      if (!useAspectWhere.get()) {
        builder.coreData.addWhere(aspectWhere(false));
      }
      builder.coreData.addFilter(ef.sameTerm(VAR_ASPECT, resAspect));
      useAspectWhere.set(true);
    });
    // Aspect type data
    builder.mapper.add("aspectType", Api.eventAspectTypeVar, aspectType);
    aspectType.map(ResourceFactory::createResource).ifPresent(resType -> {
      if (!useAspectWhere.get()) {
        builder.coreData.addWhere(aspectWhere(false));
      }
      builder.coreData.addWhere(VAR_ASPECT, RDF.type, resType);
      useAspectWhere.set(true);
    });
    // Aspect use type data
    builder.mapper.add("aspectUseType", Api.eventAspectUseTypeVar, aspectUseType);
    aspectUseType.map(ResourceFactory::createResource).ifPresent(resType -> {
      builder.coreData.addWhere(VAR_MAIN, resType, VAR_ASPECT);
    });

    // Author data
    builder.mapper.add("author", Api.eventAuthorVar, author);
    author.map(ResourceFactory::createResource).ifPresent(resAuthor -> {
      builder.coreData
          .addWhere(actWhere(false))
          .addFilter(ef.sameTerm(VAR_AUTHOR, resAuthor));
    });

    // Dates data
    if (hasDateSort || dates.isPresent()) {
      builder.coreData.addWhere(datesWhere(order, VAR_DATE_REAL_SORT, VAR_DATE));
    }
    builder.mapper.add("dates", Api.eventDatesVar, dates);
    dates.ifPresent(datesString -> {
      DateRange dateRange = CONVERTER.convert(datesString);
      Optional<Expr> start = dateRange.getStart().map(
          date -> ef.asExpr(ResourceFactory.createTypedLiteral(
              date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), XSDDateType.XSDdateTime)));
      Optional<Expr> end = dateRange.getEnd().map(
          date -> ef.asExpr(ResourceFactory.createTypedLiteral(
              date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), XSDDateType.XSDdateTime)));
      if (start.isPresent() || end.isPresent()) {
        if (start.isPresent() && end.isPresent()) {
          builder.coreData
              .addFilter(ef.ge(VAR_DATE, start.get()))
              .addFilter(ef.le(VAR_DATE, end.get()));
        } else if (start.isPresent() && dateRange.isRange()) {
          builder.coreData
              .addFilter(ef.ge(VAR_DATE, start.get()));
        } else if (start.isPresent()) {
          builder.coreData
              .addFilter(ef.sameTerm(VAR_DATE, start.get()));
        } else {
          builder.coreData
              .addFilter(ef.le(VAR_DATE, end.get()));
        }
      }
    });

    builder.extendedData
        .addWhere(datesWhere(order, VAR_DATE_REAL_SORT_OUTER, VAR_DATE_OUTER))
        .addWhere(actWhere(false))
        .addWhere(participantWhere(false))
        .addOptional(aspectWhere(false))
        .addOptional(placeWhere(false));
    return build(builder, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder =
        new HydraSingleBuilder(jenaService, individualsUri(Core.event, id), Core.event);
    builder.coreData
        .addWhere(datesWhere(Order.ASCENDING, VAR_DATE_REAL_SORT, VAR_DATE))
        .addWhere(actWhere(true))
        .addWhere(participantWhere(true))
        .addOptional(aspectWhere(true))
        .addOptional(placeWhere(true));
    return build(builder, lang);
  }

  private String build(AbstractHydraBuilder builder, Lang lang) {
    builder.build(ROW_MAPPER);
    return serialize(builder.model, lang, builder.root);
  }

  private WhereBuilder actWhere(boolean withTypes) {
    WhereBuilder builder = new WhereBuilder();
    ExprFactory ef = builder.getExprFactory();
    builder
        .addWhere(VAR_MAIN, Core.isInterpretationOf, VAR_ACT)
        .addWhere(VAR_ACT, Core.isAuthoredBy, VAR_AUTHOR)
        .addWhere(VAR_AUTHOR, RDFS.label, VAR_AUTHOR_LABEL)
        .addWhere(VAR_ACT, Core.isAuthoredOn, VAR_ACT_DATE)
        .addWhere(VAR_ACT_DATE, Core.hasDateTime, VAR_ACT_DATE_TIME)
        .addWhere(VAR_ACT, Core.hasSourceLocation, VAR_LOCATION)
        .addWhere(VAR_LOCATION, Core.hasText, VAR_LOCATION_TEXT)
        .addWhere(VAR_LOCATION, Core.hasSource, VAR_SOURCE)
        .addWhere(VAR_SOURCE, RDFS.label, VAR_SOURCE_LABEL);
    if (withTypes) {
      builder
          .addWhere(VAR_LOCATION, RDF.type, VAR_LOCATION_TYPE)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TYPE), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TYPE), RDF.getURI())))
          .addWhere(VAR_LOCATION, VAR_LOCATION_TEXT_TYPE, VAR_LOCATION_TEXT)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TEXT_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TEXT_TYPE), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOCATION_TEXT_TYPE), RDF.getURI())))
          .addFilter(ef.not(ef.sameTerm(VAR_LOCATION_TEXT_TYPE, Core.hasValue)));
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
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.takesPlaceNotLaterThan, VAR_DATE_LATEST)
            .addWhere(VAR_DATE_LATEST, Core.hasDateTime, VAR_DATE_TIME_LATEST))
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.hasSortingDate, VAR_DATE_SORT)
            .addWhere(VAR_DATE_SORT, Core.hasDateTime, VAR_DATE_TIME_SORT))
        .addBind(
            ef.cond(ef.bound(VAR_DATE_SORT), ef.asVar(VAR_DATE_SORT),
                ef.cond(ef.bound(VAR_DATE_EXACT), ef.asVar(VAR_DATE_EXACT),
                    order.equals(Order.ASCENDING)
                        ? ef.asExpr(ef.cond(ef.bound(VAR_DATE_LATEST), ef.asVar(VAR_DATE_LATEST),
                            ef.cond(ef.bound(VAR_DATE_EARLIEST), ef.asVar(VAR_DATE_EARLIEST),
                                ef.bnode())))
                        : ef.asExpr(
                            ef.cond(ef.bound(VAR_DATE_EARLIEST), ef.asVar(VAR_DATE_EARLIEST),
                                ef.cond(ef.bound(VAR_DATE_LATEST), ef.asVar(VAR_DATE_LATEST),
                                    ef.bnode()))))),
            varDate)
        .addBind(
            ef.cond(ef.bound(VAR_DATE_TIME_SORT), ef.asVar(VAR_DATE_TIME_SORT),
                ef.cond(ef.bound(VAR_DATE_TIME_EXACT), ef.asVar(VAR_DATE_TIME_EXACT),
                    order.equals(Order.ASCENDING)
                        ? ef.asExpr(ef.cond(ef.bound(VAR_DATE_TIME_LATEST),
                            ef.asVar(VAR_DATE_TIME_LATEST),
                            ef.cond(ef.bound(VAR_DATE_TIME_EARLIEST),
                                ef.asVar(VAR_DATE_TIME_EARLIEST),
                                ef.asExpr(
                                    ResourceFactory.createTypedLiteral(POSITIVE_DEFAULT_DATE,
                                        XSDDatatype.XSDdateTime)))))
                        : ef.asExpr(ef.cond(ef.bound(VAR_DATE_TIME_EARLIEST),
                            ef.asVar(VAR_DATE_TIME_EARLIEST),
                            ef.cond(ef.bound(VAR_DATE_TIME_LATEST),
                                ef.asVar(VAR_DATE_TIME_LATEST),
                                ef.asExpr(
                                    ResourceFactory.createTypedLiteral(NEGATIVE_DEFAULT_DATE,
                                        XSDDatatype.XSDdateTime))))))),
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
        .addWhere(VAR_PLACE, RDFS.label, VAR_PLACE_LABEL);
    if (withTypes) {
      builder
          .addWhere(VAR_PLACE, RDF.type, VAR_PLACE_TYPE)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PLACE_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PLACE_TYPE), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_PLACE_TYPE), RDF.getURI())));
    }
    return builder;
  }
}
