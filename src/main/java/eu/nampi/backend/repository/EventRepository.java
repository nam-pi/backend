package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_MAIN;
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

  private static final Node VAR_ASPECT = NodeFactory.createVariable("aspect");
  private static final Node VAR_ASPECT_LABEL = NodeFactory.createVariable("aspectLabel");
  private static final Node VAR_ASPECT_STRING = NodeFactory.createVariable("aspectString");
  private static final Node VAR_DATE = NodeFactory.createVariable("date");
  private static final Node VAR_DATE_OUTER = NodeFactory.createVariable("dateOuter");
  private static final Node VAR_DATE_EARLIEST = NodeFactory.createVariable("dateEarliest");
  private static final Node VAR_DATE_EXACT = NodeFactory.createVariable("dateExact");
  private static final Node VAR_DATE_LATEST = NodeFactory.createVariable("dateLatest");
  private static final Node VAR_DATE_REAL_SORT = NodeFactory.createVariable("dateRealSort");
  private static final Node VAR_DATE_REAL_SORT_OUTER =
      NodeFactory.createVariable("dateRealSortOuter");
  private static final Node VAR_DATE_SORT = NodeFactory.createVariable("dateSort");
  private static final Node VAR_DATE_TIME_EARLIEST = NodeFactory.createVariable("dateTimeEarliest");
  private static final Node VAR_DATE_TIME_EXACT = NodeFactory.createVariable("dateTimeExact");
  private static final Node VAR_DATE_TIME_LATEST = NodeFactory.createVariable("dateTimeLatest");
  private static final Node VAR_DATE_TIME_SORT = NodeFactory.createVariable("dateTimeSort");
  private static final Node VAR_PARTICIPANT = NodeFactory.createVariable("participant");
  private static final Node VAR_PARTICIPANT_LABEL = NodeFactory.createVariable("participantLabel");
  private static final Node VAR_PLACE = NodeFactory.createVariable("place");
  private static final Node VAR_PLACE_LABEL = NodeFactory.createVariable("placeLabel");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    model.add(main, RDF.type, Core.event);
    // Label
    Optional.ofNullable(row.getLiteral(VAR_LABEL.toString())).map(Literal::getString)
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional.ofNullable(row.getLiteral(VAR_COMMENT.toString())).map(Literal::getString)
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // Aspect
    Optional.ofNullable(row.getResource(VAR_ASPECT.toString()))
        .ifPresent(aspect -> {
          model
              .add(main, Core.usesAspect, aspect)
              .add(aspect, RDF.type, Core.aspect);
          Optional.ofNullable(row.getLiteral(VAR_ASPECT_LABEL.toString()))
              .ifPresent(label -> model.add(aspect, RDFS.label, label));
          Optional.ofNullable(row.getLiteral(VAR_ASPECT_STRING.toString()))
              .ifPresent(str -> model.add(aspect, Core.hasXsdString, str));
        });
    // Participant
    Optional.ofNullable(row.getResource(VAR_PARTICIPANT.toString()))
        .ifPresent(agent -> model
            .add(main, Core.hasParticipant, agent)
            .add(agent, RDF.type, Core.agent)
            .add(agent, RDFS.label, row.getLiteral(VAR_PARTICIPANT_LABEL.toString())));
    // Place
    Optional.ofNullable(row.getResource(VAR_PLACE.toString()))
        .ifPresent(place -> model
            .add(main, Core.takesPlaceAt, place)
            .add(place, RDF.type, Core.place)
            .add(place, RDFS.label, row.getLiteral(VAR_PLACE_LABEL.toString())));
    // Exact date
    Optional.ofNullable(row.getLiteral(VAR_DATE_TIME_EXACT.toString())).ifPresent(dateTime -> {
      Resource resDate = row.getResource(VAR_DATE_EXACT
          .toString());
      model
          .add(main, Core.takesPlaceOn, resDate)
          .add(resDate, RDF.type, Core.date)
          .add(resDate, Core.hasXsdDateTime, dateTime);
    });
    // Earliest date
    Optional.ofNullable(row.getLiteral(VAR_DATE_TIME_EARLIEST.toString())).ifPresent(dateTime -> {
      Resource resDate = row.getResource(VAR_DATE_EARLIEST.toString());
      model
          .add(main, Core.takesPlaceNotEarlierThan, resDate)
          .add(resDate, RDF.type, Core.date)
          .add(resDate, Core.hasXsdDateTime, dateTime);
    });
    // Latest date
    Optional.ofNullable(row.getLiteral(VAR_DATE_TIME_LATEST.toString())).ifPresent(dateTime -> {
      Resource resDate = row.getResource(VAR_DATE_LATEST.toString());
      model
          .add(main, Core.takesPlaceNotLaterThan, resDate)
          .add(resDate, RDF.type, Core.date)
          .add(resDate, Core.hasXsdDateTime, dateTime);
    });
    // Sorting date
    Optional.ofNullable(row.getLiteral(VAR_DATE_OUTER.toString())).ifPresent(dateTime -> {
      Resource resDate = row.getResource(VAR_DATE_REAL_SORT_OUTER.toString());
      model
          .add(main, Core.hasSortingDate, resDate)
          .add(resDate, RDF.type, Core.date)
          .add(resDate, Core.hasXsdDateTime, dateTime);
    });
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #dates,#aspect, #aspectType, #aspectUseType, #participant, #participantType, #participationType, #place}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> dates,
      Optional<String> aspect, Optional<String> aspectType, Optional<String> aspectUseType,
      Optional<String> participant, Optional<String> participantType,
      Optional<String> participationType, Optional<String> place) {

    HydraCollectionBuilder builder = new HydraCollectionBuilder(jenaService, endpointUri("events"),
        Core.event, Api.eventOrderByVar, params);
    ExprFactory ef = builder.ef;

    boolean hasDateSort = params.getOrderByClauses().containsKey("date");
    Order order = params.getOrderByClauses().getOrderFor("date").orElse(Order.ASCENDING);

    // Place data
    builder.mapper.add("place", Api.eventPlaceVar, place);
    place.map(ResourceFactory::createResource).ifPresent(resPlace -> {
      builder.coreData
          .addWhere(placeWhere())
          .addFilter(ef.sameTerm(VAR_PLACE, resPlace));
    });

    AtomicBoolean useParticipantWhere = new AtomicBoolean(false);
    // Participant data
    builder.mapper.add("participant", Api.eventParticipantVar, participant);
    participant.map(ResourceFactory::createResource).ifPresent(resParticipant -> {
      if (!useParticipantWhere.get()) {
        builder.coreData.addWhere(participantWhere());
      }
      builder.coreData.addFilter(ef.sameTerm(VAR_PARTICIPANT, resParticipant));
      useParticipantWhere.set(true);
    });
    // Participant type data
    builder.mapper.add("participantType", Api.eventParticipantTypeVar, participantType);
    participantType.map(ResourceFactory::createResource).ifPresent(resType -> {
      if (!useParticipantWhere.get()) {
        builder.coreData.addWhere(participantWhere());
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
        builder.coreData.addWhere(aspectWhere());
      }
      builder.coreData.addFilter(ef.sameTerm(VAR_ASPECT, resAspect));
      useAspectWhere.set(true);
    });
    // Aspect type data
    builder.mapper.add("aspectType", Api.eventAspectTypeVar, aspectType);
    aspectType.map(ResourceFactory::createResource).ifPresent(resType -> {
      if (!useAspectWhere.get()) {
        builder.coreData.addWhere(aspectWhere());
      }
      builder.coreData.addWhere(VAR_ASPECT, RDF.type, resType);
      useAspectWhere.set(true);
    });
    // Aspect use type data
    builder.mapper.add("aspectUseType", Api.eventAspectUseTypeVar, aspectUseType);
    aspectUseType.map(ResourceFactory::createResource).ifPresent(resType -> {
      builder.coreData.addWhere(VAR_MAIN, resType, VAR_ASPECT);
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
        .addWhere(participantWhere())
        .addOptional(aspectWhere())
        .addOptional(placeWhere())
        .addWhere(datesWhere(order, VAR_DATE_REAL_SORT_OUTER, VAR_DATE_OUTER));
    return build(builder, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder =
        new HydraSingleBuilder(jenaService, individualsUri(Core.event, id), Core.event);
    builder.coreData
        .addWhere(participantWhere())
        .addOptional(aspectWhere())
        .addOptional(placeWhere())
        .addWhere(datesWhere(Order.ASCENDING, VAR_DATE_REAL_SORT, VAR_DATE));
    return build(builder, lang);
  }

  private String build(AbstractHydraBuilder builder, Lang lang) {
    builder.build(ROW_MAPPER);
    return serialize(builder.model, lang, builder.root);
  }

  private WhereBuilder aspectWhere() {
    return new WhereBuilder()
        .addWhere(VAR_MAIN, Core.usesAspect, VAR_ASPECT)
        .addWhere(VAR_ASPECT, RDFS.label, VAR_ASPECT_LABEL)
        .addOptional(VAR_ASPECT, Core.hasXsdString, VAR_ASPECT_STRING);
  }

  private WhereBuilder datesWhere(Order order, Node varDate, Node varDateTime) {
    WhereBuilder builder = new WhereBuilder();
    ExprFactory ef = builder.getExprFactory();
    builder
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.takesPlaceOn, VAR_DATE_EXACT)
            .addWhere(VAR_DATE_EXACT, Core.hasXsdDateTime, VAR_DATE_TIME_EXACT))
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.takesPlaceNotEarlierThan, VAR_DATE_EARLIEST)
            .addWhere(VAR_DATE_EARLIEST, Core.hasXsdDateTime, VAR_DATE_TIME_EARLIEST))
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.takesPlaceNotLaterThan, VAR_DATE_LATEST)
            .addWhere(VAR_DATE_LATEST, Core.hasXsdDateTime, VAR_DATE_TIME_LATEST))
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.hasSortingDate, VAR_DATE_SORT)
            .addWhere(VAR_DATE_SORT, Core.hasXsdDateTime, VAR_DATE_TIME_SORT))
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
                                ef.asExpr(ResourceFactory.createTypedLiteral(NEGATIVE_DEFAULT_DATE,
                                    XSDDatatype.XSDdateTime))))))),
            varDateTime);
    return builder;
  }

  private WhereBuilder participantWhere() {
    return new WhereBuilder()
        .addWhere(VAR_MAIN, Core.hasParticipant, VAR_PARTICIPANT)
        .addWhere(VAR_PARTICIPANT, RDFS.label, VAR_PARTICIPANT_LABEL);
  }

  private WhereBuilder placeWhere() {
    return new WhereBuilder()
        .addWhere(VAR_MAIN, Core.takesPlaceAt, VAR_PLACE)
        .addWhere(VAR_PLACE, RDFS.label, VAR_PLACE_LABEL);
  }
}
