package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.MAIN_SUBJ;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.converter.StringToDateRangeConverter;
import eu.nampi.backend.model.DateRange;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.AbstractHydraBuilder;
import eu.nampi.backend.model.hydra.HydraSingleBuilder;
import eu.nampi.backend.model.hydra.ParameterMapper;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;
import eu.nampi.backend.vocabulary.Hydra;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
  private static final Node VAR_MAIN = NodeFactory.createVariable("main");
  private static final Node VAR_MAIN_LABEL = NodeFactory.createVariable("mainLabel");
  private static final Node VAR_FIRST = NodeFactory.createVariable("first");
  private static final Node VAR_LAST = NodeFactory.createVariable("last");
  private static final Node VAR_MANAGES = NodeFactory.createVariable("manages");
  private static final Node VAR_NEXT = NodeFactory.createVariable("next");
  private static final Node VAR_PARTICIPANT = NodeFactory.createVariable("participant");
  private static final Node VAR_PARTICIPANT_LABEL = NodeFactory.createVariable("participantLabel");
  private static final Node VAR_PLACE = NodeFactory.createVariable("place");
  private static final Node VAR_PLACE_LABEL = NodeFactory.createVariable("placeLabel");
  private static final Node VAR_PREVIOUS = NodeFactory.createVariable("previous");
  private static final Node VAR_SEARCH = NodeFactory.createVariable("search");
  private static final Node VAR_TOTAL_ITEMS = NodeFactory.createVariable("totalItems");

  public Model findAll(QueryParameters params, Optional<String> dates, Optional<String> aspect,
      Optional<String> aspectType, Optional<String> aspectUseType, Optional<String> participant,
      Optional<String> participantType, Optional<String> participationType, Optional<String> place) {

    ConstructBuilder construct = new ConstructBuilder();
    try {
      String baseUri = endpointUri("events");

      Node baseNode = NodeFactory.createURI(baseUri);

    // @formatter:off
      construct
        .addPrefix("core", Core.getURI())
        .addPrefix("doc", Doc.getURI())
        .addPrefix("hydra", Hydra.getURI())
        .addPrefix("rdf", RDF.getURI())
        .addPrefix("rdfs", RDFS.getURI())
        .addPrefix("xsd", XSD.getURI());

      ExprFactory ef = construct.getExprFactory();

      WhereBuilder labelWhere = new WhereBuilder()
        .addWhere(VAR_MAIN, RDFS.label, VAR_MAIN_LABEL);

      WhereBuilder datesWhere = new WhereBuilder()
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
            ef.cond(ef.bound(VAR_DATE_EXACT), ef.asVar(VAR_DATE_EXACT), params.getOrderByClauses().getOrderFor("date").map(o -> o.equals(Order.ASCENDING)
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
            ef.cond(ef.bound(VAR_DATE_TIME_EXACT), ef.asVar(VAR_DATE_TIME_EXACT), params.getOrderByClauses().getOrderFor("date").map(o -> o.equals(Order.ASCENDING)
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

      WhereBuilder placeWhere = new WhereBuilder()
        .addOptional(new WhereBuilder()
          .addWhere(VAR_MAIN, Core.takesPlaceAt, VAR_PLACE)
          .addWhere(VAR_PLACE, RDFS.label, VAR_PLACE_LABEL));
      
      WhereBuilder participantWhere = new WhereBuilder()
        .addWhere(VAR_MAIN, Core.hasParticipant, VAR_PARTICIPANT)
        .addWhere(VAR_PARTICIPANT, RDFS.label, VAR_PARTICIPANT_LABEL);

      WhereBuilder aspectWhere = new WhereBuilder()
        .addOptional(new WhereBuilder()
          .addWhere(VAR_MAIN, Core.usesAspect, VAR_ASPECT)
          .addWhere(VAR_ASPECT, RDFS.label, VAR_ASPECT_LABEL)
          .addOptional(VAR_ASPECT, Core.hasXsdString, VAR_ASPECT_STRING));

      // Select the count data
      WhereBuilder countWhere = new WhereBuilder()
        .addWhere(VAR_MAIN, RDF.type, Core.event);
      // Select the actual data
      WhereBuilder dataWhere = new WhereBuilder()
        .addWhere(VAR_MAIN, RDF.type, Core.event);

      // Participant data
      if(participant.isPresent()) {
        Node varParticipant = NodeFactory.createVariable("filterParticipant");
        Resource participantResource = ResourceFactory.createResource(participant.get());
        Expr sameTerm = ef.sameTerm(varParticipant, participantResource);
        dataWhere
          .addWhere(VAR_MAIN, Core.hasParticipant, varParticipant)
          .addFilter(sameTerm);
        countWhere
          .addWhere(VAR_MAIN, Core.hasParticipant, varParticipant)
          .addFilter(sameTerm);
      }
      if(participantType.isPresent()) {
        Node varParticipant = NodeFactory.createVariable("filterParticipantTypeParticipant");
        Resource typeResource = ResourceFactory.createResource(participantType.get());
        dataWhere
          .addWhere(VAR_MAIN, Core.hasParticipant, varParticipant)
          .addWhere(varParticipant, RDF.type, typeResource);
        countWhere
          .addWhere(VAR_MAIN, Core.hasParticipant, varParticipant)
          .addWhere(varParticipant, RDF.type, typeResource);
      }
      if(participationType.isPresent()) {
        Node varType = NodeFactory.createVariable("filterParticipationType");
        Property type = ResourceFactory.createProperty(participationType.get());
        dataWhere
          .addWhere(VAR_MAIN, type, varType);
        countWhere
          .addWhere(VAR_MAIN, type, varType);
      }
      dataWhere.addWhere(participantWhere);

      // Text data
      if(params.getText().isPresent()) {
        Expr regex = ef.regex(VAR_MAIN_LABEL, params.getText().get(), "i");
        dataWhere
          .addFilter(regex);
        countWhere
          .addWhere(labelWhere)
          .addFilter(regex);
      }
      dataWhere.addWhere(labelWhere);

      // Dates data
      if (dates.isPresent()) {
        String datesString = dates.get();
        DateRange dateRange = CONVERTER.convert(datesString);
        Optional<String> start = dateRange.getStart().map(date -> "'" + date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "'^^xsd:dateTime");
        Optional<String> end = dateRange.getEnd().map(date -> "'" + date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "'^^xsd:dateTime");
        if(start.isPresent() || end.isPresent()) {
          countWhere.addWhere(datesWhere);
          if (start.isPresent() && end.isPresent()) {
            dataWhere.addFilter(ef.ge(VAR_DATE, start.get()));
            dataWhere.addFilter(ef.le(VAR_DATE, end.get()));
            countWhere.addFilter(ef.ge(VAR_DATE, start.get()));
            countWhere.addFilter(ef.le(VAR_DATE, end.get()));
          } else if (start.isPresent() && dateRange.isRange()) {
            dataWhere.addFilter(ef.ge(VAR_DATE, start.get()));
            countWhere.addFilter(ef.ge(VAR_DATE, start.get()));
          } else if (start.isPresent()) {
            dataWhere.addFilter(ef.sameTerm(VAR_DATE, start.get()));
            countWhere.addFilter(ef.sameTerm(VAR_DATE, start.get()));
          } else {
            dataWhere.addFilter(ef.le(VAR_DATE, end.get()));
            countWhere.addFilter(ef.le(VAR_DATE, end.get()));
          }
        }
      }
      dataWhere.addWhere(datesWhere);

      // Type data
      if(params.getType().isPresent()) {
        Resource typeResource = ResourceFactory.createResource(params.getType().get());
        dataWhere.addWhere(VAR_MAIN, RDF.type, typeResource);
        countWhere.addWhere(VAR_MAIN, RDF.type, typeResource);
      }

      // Place data
      if(place.isPresent()) {
        Expr sameTerm = ef.sameTerm(VAR_PLACE, ResourceFactory.createResource(place.get()));
        dataWhere
          .addFilter(sameTerm);
        countWhere
          .addWhere(placeWhere)
          .addFilter(sameTerm);
      }
      dataWhere.addWhere(placeWhere);

      // Aspect data
      if(aspect.isPresent()){
        Expr sameTerm = ef.sameTerm(VAR_ASPECT, ResourceFactory.createResource(aspect.get()));
        dataWhere
          .addFilter(sameTerm);
        countWhere
          .addWhere(aspectWhere)
          .addFilter(sameTerm);
      }
      if(aspectType.isPresent()) {
        Node varAspect = NodeFactory.createVariable("filterAspectTypeAspect");
        Property type = ResourceFactory.createProperty(aspectType.get());
        dataWhere
          .addWhere(VAR_MAIN, Core.usesAspect, varAspect)
          .addWhere(varAspect, RDF.type, type);
        countWhere
          .addWhere(VAR_MAIN, Core.usesAspect, varAspect)
          .addWhere(varAspect, RDF.type, type);
      }
      if(aspectUseType.isPresent()) {
        Node varType = NodeFactory.createVariable("filterAspectUseType");
        Property type = ResourceFactory.createProperty(aspectUseType.get());
        dataWhere
          .addWhere(VAR_MAIN, type, varType);
        countWhere
          .addWhere(VAR_MAIN, type, varType);
      }
      dataWhere.addWhere(aspectWhere);

      // Construct the result
      construct
        // Add general hydra data
        .addConstruct(baseNode, RDF.type, Hydra.Collection)
        .addConstruct(baseNode, Hydra.totalItems, VAR_TOTAL_ITEMS)
        .addConstruct(baseNode, Hydra.manages, VAR_MANAGES)
        .addConstruct(VAR_MANAGES, Hydra.object, Core.event)
        // Add search
        .addConstruct(baseNode, Hydra.search, VAR_SEARCH )
        .addConstruct(VAR_SEARCH, RDF.type, Hydra.IriTemplate)
        .addConstruct(VAR_SEARCH, Hydra.variableRepresentation, Hydra.BasicRepresentation)
        // Add event data
        .addConstruct(baseNode, Hydra.member, VAR_MAIN)
        .addConstruct(VAR_MAIN, RDF.type, Core.event)
        .addConstruct(VAR_MAIN, RDFS.label, VAR_MAIN_LABEL)
        // Participant
        .addConstruct(VAR_MAIN, Core.hasParticipant, VAR_PARTICIPANT)
        .addConstruct(VAR_PARTICIPANT, RDF.type, Core.person)
        .addConstruct(VAR_PARTICIPANT, RDFS.label, VAR_PARTICIPANT_LABEL)
        // Aspect
        .addConstruct(VAR_MAIN, Core.usesAspect, VAR_ASPECT)
        .addConstruct(VAR_ASPECT, RDF.type, Core.aspect)
        .addConstruct(VAR_ASPECT, RDFS.label, VAR_ASPECT_LABEL)
        .addConstruct(VAR_ASPECT, Core.hasXsdString, VAR_ASPECT_STRING)
        // Place
        .addConstruct(VAR_MAIN, Core.takesPlaceAt, VAR_PLACE)
        .addConstruct(VAR_PLACE, RDF.type, Core.place)
        .addConstruct(VAR_PLACE, RDF.type, VAR_PLACE_LABEL)
        // Exact date
        .addConstruct(VAR_MAIN, Core.takesPlaceOn, VAR_DATE_EXACT)
        .addConstruct(VAR_DATE_EXACT, RDF.type, Core.date)
        .addConstruct(VAR_DATE_EXACT, Core.hasXsdDateTime, VAR_DATE_TIME_EXACT)
        // Earliest date
        .addConstruct(VAR_MAIN, Core.takesPlaceNotEarlierThan, VAR_DATE_EARLIEST)
        .addConstruct(VAR_DATE_EARLIEST, RDF.type, Core.date)
        .addConstruct(VAR_DATE_EARLIEST, Core.hasXsdDateTime, VAR_DATE_TIME_EARLIEST)
        // Latest date
        .addConstruct(VAR_MAIN, Core.takesPlaceNotLaterThan, VAR_DATE_LATEST)
        .addConstruct(VAR_DATE_LATEST, RDF.type, Core.date)
        .addConstruct(VAR_DATE_LATEST, Core.hasXsdDateTime, VAR_DATE_TIME_LATEST)
        // Sort date
        .addConstruct(VAR_MAIN, Core.hasSortingDate, VAR_DATE_REAL_SORT)
        .addConstruct(VAR_DATE_REAL_SORT, RDF.type, Core.date)
        .addConstruct(VAR_DATE_REAL_SORT, Core.hasXsdDateTime, VAR_DATE);

      // Add all variable bindings
      WhereBuilder bindWhere = new WhereBuilder()
        .addBind(ef.bnode(), VAR_SEARCH)
        .addBind(ef.bnode(), VAR_MANAGES);

      // Set up selects
      SelectBuilder dataSelect = new SelectBuilder()
        .addVar("*")
        .addWhere(dataWhere);
        params.getOrderByClauses().appendAllTo(dataSelect);
        dataSelect.addOrderBy(VAR_MAIN)
        .setOffset(params.getOffset())
        .setLimit(params.getLimit());
      SelectBuilder countSelect = new SelectBuilder()
        .addVar("count(*)", VAR_TOTAL_ITEMS)
        .addWhere(countWhere);
      SelectBuilder contentSelect = new SelectBuilder()
        .addVar("*")
        .addUnion(dataSelect)
        .addUnion(countSelect);
      SelectBuilder bindSelect = new SelectBuilder()
        .addVar("*")
        .addWhere(bindWhere);

      ParameterMapper mapper = new ParameterMapper(baseUri, VAR_SEARCH, construct, bindSelect);
      Node view = mapper
        .add("limit", Hydra.limit, params.getLimit())
        .add("offset", Hydra.offset, params.getOffset())
        .add("orderBy", Doc.eventOrderByVar, params.getOrderByClauses().toQueryString())
        .add("pageIndex", Hydra.pageIndex, null)
        .add("text", Doc.textVar, params.getText().orElse(""))
        .add("type", RDF.type, params.getType().orElse(""))
        .add("dates", Doc.eventDatesVar, dates.orElse(""))
        .add("place", Doc.eventPlaceVar, place.orElse(""))
        .add("participant", Doc.eventParticipantVar, participant.orElse(""))
        .add("participantType", Doc.eventParticipantTypeVar, participantType.orElse(""))
        .add("participationType", Doc.eventParticipationTypeVar, participationType.orElse(""))
        .add("aspect", Doc.eventAspectVar, aspect.orElse(""))
        .add("aspectType", Doc.eventAspectTypeVar, aspect.orElse(""))
        .add("aspectUseType", Doc.eventAspectUseTypeVar, aspect.orElse(""))
        .addTemplate(baseNode);

      construct
        .addConstruct(view, Hydra.first, VAR_FIRST)
        .addConstruct(view, Hydra.previous, VAR_PREVIOUS)
        .addConstruct(view, Hydra.next, VAR_NEXT)
        .addConstruct(view, Hydra.last, VAR_LAST)
        .addWhere(new WhereBuilder()
        .addUnion(contentSelect)
        .addUnion(bindSelect))
        .addBind("if(contains('" + view + "', 'offset=0'), 1+'', replace('" + view + "', 'offset=\\\\d*', 'offset=0'))", VAR_FIRST)
        .addBind("if(" + params.getOffset() + " >= floor(" + VAR_TOTAL_ITEMS + " / " + params.getLimit() + ") * " + params.getLimit() + " , 1+'', replace('" + view + "', 'offset=\\\\d*', concat('offset=', str(xsd:integer(floor(" + VAR_TOTAL_ITEMS + " / " + params.getLimit() + ") * " + params.getLimit() + ")))))", VAR_LAST)
        .addBind("if(" + (params.getOffset() - params.getLimit()) + " >= 0, iri(replace('" + view + "', 'offset=\\\\d*', concat('offset=', str(" + (params.getOffset() - params.getLimit()) + ")))), 1+'')", VAR_PREVIOUS)
        .addBind("if(" + (params.getOffset() + params.getLimit()) + " < " + VAR_TOTAL_ITEMS + ", replace('" + view + "', 'offset=\\\\d*', concat('offset=', str(" + (params.getOffset() + params.getLimit()) + "))), 1+'')", VAR_NEXT);

    // @formatter:on
    } catch (ParseException e) {
      log.error(e.getMessage());
    }
    return construct(construct);
  }

  // public Model findAll(QueryParameters params, Optional<String> dates,
  // Optional<String> aspect,
  // Optional<String> aspectType, Optional<String> aspectUseType, Optional<String>
  // participant,
  // Optional<String> participantType, Optional<String> participationType,
  // Optional<String> place) {
  // HydraCollectionBuilder hydra =
  // new HydraCollectionBuilder(params, Core.event, Doc.eventOrderByVar);
  //   // @formatter:off
  //   place.ifPresentOrElse(pl -> hydra
  //       .addMainWhere(Core.takesPlaceAt, "<" + pl + ">")
  //       .addSearchVariable("place", Doc.eventParticipantVar, false, "'" + pl + "'")
  //     , () -> hydra
  //       .addSearchVariable("place", Doc.eventParticipantVar, false));
  //   participant.ifPresentOrElse(p -> hydra
  //       .addMainWhere(Core.hasParticipant, "<" + p + ">")
  //       .addSearchVariable("participant", Doc.eventParticipantVar, false, "'" + p + "'")
  //     , () -> hydra
  //       .addSearchVariable("participant", Doc.eventParticipantVar, false));
  //   participantType.ifPresentOrElse(pt -> hydra
  //       .addMainWhere(PathFactory.pathSeq(PathFactory.pathLink(Core.hasParticipant.asNode()), PathFactory.pathLink(RDF.type.asNode())), "<" + pt + ">")
  //       .addSearchVariable("participantType", Doc.eventParticipantTypeVar, false, "'" + pt + "'")
  //     , () -> hydra
  //       .addSearchVariable("participantType", Doc.eventParticipantTypeVar, false));
  //   participationType.ifPresentOrElse(pt -> hydra
  //       .addMainWhere("<" + pt + ">", "?p")
  //       .addWhere("?p", RDF.type, Core.agent)
  //       .addSearchVariable("participationType", Doc.eventParticipationTypeVar, false, "'" + pt + "'")
  //     , () -> hydra
  //       .addSearchVariable("participationType", Doc.eventParticipationTypeVar, false));
  //   aspect.ifPresentOrElse(a -> hydra
  //       .addMainWhere(Core.usesAspect, "<" + a + ">")
  //       .addSearchVariable("aspect", Doc.eventAspectVar, false, "'" + a + "'")
  //     , () -> hydra
  //       .addSearchVariable("aspect", Doc.eventAspectVar, false));
  //   aspectType.ifPresentOrElse(at -> hydra
  //       .addMainWhere(PathFactory.pathSeq(PathFactory.pathLink(Core.usesAspect.asNode()), PathFactory.pathLink(RDF.type.asNode())), "<" + at + ">")
  //       .addSearchVariable("aspectType", Doc.eventAspectTypeVar, false, "'" + at + "'")
  //     , () -> hydra
  //       .addSearchVariable("aspectType", Doc.eventAspectTypeVar, false));
  //   aspectUseType.ifPresentOrElse(aut -> hydra
  //       .addMainWhere(PathFactory.pathSeq(PathFactory.pathLink(ResourceFactory.createProperty(aut).asNode()), PathFactory.pathLink(RDF.type.asNode())), Core.aspect)
  //       .addSearchVariable("aspectUseType", Doc.eventAspectUseTypeVar, false, "'" + aut + "'")
  //     , () -> hydra
  //       .addSearchVariable("aspectUseType", Doc.eventAspectUseTypeVar, false));
  //   addData(hydra);
  //   hydra
  //     .addBind( "if(bound(?sortingDate), ?sortingDate, if(bound(?exactDate), ?exactDate, if(bound(?earliestDate), ?earliestDate, if(bound(?latestDate), ?latestDate, bnode()))))", "?realSortingDate")
  //     .addBind( "if(bound(?sortingDateTime), ?sortingDateTime, if(bound(?exactDateTime), ?exactDateTime, if(bound(?earliestDateTime), ?earliestDateTime, if(bound(?latestDateTime), ?latestDateTime, '" + (params.getOrderByClauses().getOrderFor("date").orElse(Order.ASCENDING) == Order.ASCENDING ? "9999-12-31T23:59:59" : "-9999-01-01:00:00:00") + "'^^xsd:dateTime))))", "?date")
  //     .addMainConstruct(Core.hasSortingDate, "?realSortingDate")
  //     .addConstruct("?realSortingDate", Core.hasXsdDateTime, "?date")
  //     .addConstruct("?realSortingDate", RDF.type, Core.date);
  //   // @formatter:on
  // dates.map(s -> CONVERTER.convert(dates.get())).ifPresentOrElse(dr -> {
  // hydra.addSearchVariable("dates", Doc.eventDatesVar, false, "'" + dates.get()
  // + "'");
  // Optional<LocalDateTime> start = dr.getStart();
  // if (start.isPresent()) {
  // hydra.addBind(
  // "'" + start.get().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
  // "'^^xsd:dateTime",
  // "?filterStart");
  // }
  // Optional<LocalDateTime> end = dr.getEnd();
  // if (end.isPresent()) {
  // hydra.addBind(
  // "'" + end.get().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
  // "'^^xsd:dateTime",
  // "?filterEnd");
  // }
  // if (start.isPresent() && end.isPresent()) {
  // hydra.addFilter("?date >= ?filterStart && ?date <= ?filterEnd");
  // } else if (start.isPresent() && dr.isRange()) {
  // hydra.addFilter("?date >= ?filterStart");
  // } else if (start.isPresent()) {
  // hydra.addFilter("?date = ?filterStart");
  // } else {
  // hydra.addFilter("?date <= ?filterEnd");
  // }
  // }, () -> {
  // hydra.addSearchVariable("dates", Doc.eventDatesVar, false);
  // });
  // return construct(hydra);
  // }

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #dates,#aspect, #aspectType, #aspectUseType, #participant, #participantType, #participationType, #place}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> dates, Optional<String> aspect,
      Optional<String> aspectType, Optional<String> aspectUseType, Optional<String> participant,
      Optional<String> participantType, Optional<String> participationType, Optional<String> place) {
    Model model = findAll(params, dates, aspect, aspectType, aspectUseType, participant, participantType,
        participationType, place);
    return serialize(model, lang, ResourceFactory.createResource(params.getBaseUrl()));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    String uri = individualsUri(Core.event, id);
    HydraSingleBuilder builder = new HydraSingleBuilder(uri, Core.event);
    addData(builder);
    builder.addMainConstruct(Core.hasSortingDate, "?sortingDate").addConstruct("?sortingDate", RDF.type, Core.date)
        .addConstruct("?sortingDate", Core.hasXsdDateTime, "?sortingDateTime");
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(uri));
  }

  private void addData(AbstractHydraBuilder<?> builder) {
    // @formatter:off
    builder
      // Person related data
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.hasMainParticipant, "?prs")
        .addWhere("?prs", RDFS.label, "?prsl"))
      .addMainConstruct(Core.hasMainParticipant, "?prs")
      .addConstruct("?prs", RDF.type, Core.person)
      .addConstruct("?prs", RDFS.label, "?prsl")
      // // Aspect related data
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.usesAspect, "?asp")
        .addWhere("?asp", RDFS.label, "?aspl"))
      .addMainConstruct(Core.usesAspect, "?asp")
      .addConstruct("?asp", RDF.type, Core.aspect)
      .addConstruct("?asp", RDFS.label, "?aspl")
      // Place related data
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceAt, "?pla")
        .addWhere("?pla", RDFS.label, "?plal"))
      .addMainConstruct(Core.takesPlaceAt, "?pla")
      .addConstruct("?pla", RDF.type, Core.place)
      .addConstruct("?pla", RDFS.label, "?plal")
      // The exact event date
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceOn, "?exactDate")
        .addWhere("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
        .addWhere("?exactDate", RDF.type, Core.date))
      .addMainConstruct(Core.takesPlaceOn, "?exactDate")
      .addConstruct("?exactDate", Core.hasXsdDateTime, "?exactDateTime")
      .addConstruct("?exactDate", RDF.type, Core.date)
      // The earliest possible event date
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceNotEarlierThan, "?earliestDate")
        .addWhere("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
        .addWhere("?earliestDate", RDF.type, Core.date))
      .addMainConstruct(Core.takesPlaceNotEarlierThan, "?earliestDate")
      .addConstruct("?earliestDate", Core.hasXsdDateTime, "?earliestDateTime")
      .addConstruct("?earliestDate", RDF.type, Core.date)
      // The latest possible event date
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.takesPlaceNotLaterThan, "?latestDate")
        .addWhere("?latestDate", Core.hasXsdDateTime, "?latestDateTime")
        .addWhere("?latestDate", RDF.type, Core.date))
      .addMainConstruct(Core.takesPlaceNotLaterThan, "?latestDate")
      // The sorting date
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, Core.hasSortingDate, "?sortingDate")
        .addWhere("?sortingDate", Core.hasXsdDateTime, "?sortingDateTime")
        .addWhere("?sortingDate", RDF.type, Core.date))
      .addConstruct("?latestDate", Core.hasXsdDateTime, "?latestDateTime")
      .addConstruct("?latestDate", RDF.type, Core.date);
    // @formatter:off
  }
}
