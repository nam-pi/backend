package eu.nampi.backend.repository;

import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_MAIN;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.queryBuilder.HydraBuilderFactory;
import eu.nampi.backend.queryBuilder.HydraCollectionBuilder;
import eu.nampi.backend.queryBuilder.HydraInsertBuilder;
import eu.nampi.backend.queryBuilder.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "acts")
public class ActRepository {

  @Autowired
  HydraBuilderFactory hydraBuilderFactory;

  private static final List<Literal> DEFAULT_LABEL =
      Arrays.asList(ResourceFactory.createLangLiteral("Document interpretation act", "en"));
  private static final String ENDPOINT_NAME = "acts";
  private static final Node VAR_AUTHOR = NodeFactory.createVariable("author");
  private static final Node VAR_AUTHOR_LABEL = NodeFactory.createVariable("authorLabel");
  private static final Node VAR_DATE = NodeFactory.createVariable("authoredDate");
  private static final Node VAR_DATE_TIME = NodeFactory.createVariable("authoredDateTime");
  private static final Node VAR_INT = NodeFactory.createVariable("interpretation");
  private static final Node VAR_INT_LABEL = NodeFactory.createVariable("interpretationLabel");
  private static final Node VAR_LOC = NodeFactory.createVariable("sourceLocation");
  private static final Node VAR_LOC_TEXT = NodeFactory.createVariable("sourceLocationText");
  private static final Node VAR_LOC_TEXT_TYPE =
      NodeFactory.createVariable("sourceLocationTextType");
  private static final Node VAR_LOC_TYPE = NodeFactory.createVariable("sourceLocationType");
  private static final Node VAR_SRC = NodeFactory.createVariable("source");
  private static final Node VAR_SRC_LABEL = NodeFactory.createVariable("sourceLabel");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    model
        .add(main, RDF.type, Core.act);
    // Label
    Optional
        .ofNullable(row.getLiteral(VAR_LABEL.toString()))
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional
        .ofNullable(row.getLiteral(VAR_COMMENT.toString()))
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // Author
    Resource resAuthor = row.getResource(VAR_AUTHOR.toString());
    model
        .add(main, Core.isAuthoredBy, resAuthor)
        .add(resAuthor, RDF.type, Core.author)
        .add(resAuthor, RDFS.label, row.getLiteral(VAR_AUTHOR_LABEL.toString()));
    // Date
    Resource resDate = row.getResource(VAR_DATE.toString());
    model
        .add(main, Core.isAuthoredOn, resDate)
        .add(resDate, RDF.type, Core.date)
        .add(resDate, Core.hasDateTime, row.getLiteral(VAR_DATE_TIME.toString()));
    // Source location
    Resource resLocation = row.getResource(VAR_LOC.toString());
    Literal litLocationText = row.getLiteral(VAR_LOC_TEXT.toString());
    model
        .add(main, Core.hasSourceLocation, resLocation)
        .add(resLocation, Core.hasText, litLocationText);
    Optional
        .ofNullable(row.getResource(VAR_LOC_TYPE.toString()))
        .ifPresentOrElse(type -> model.add(resLocation, RDF.type, type),
            () -> model.add(resLocation, RDF.type, Core.sourceLocation));
    Optional
        .ofNullable(row.getResource(VAR_LOC_TEXT_TYPE.toString()))
        .map(type -> ResourceFactory.createProperty(type.getURI()))
        .ifPresent(type -> model.add(resLocation, type, litLocationText));
    // Source
    Resource resSource = row.getResource(VAR_SRC.toString());
    model.add(resLocation, Core.hasSource, resSource)
        .add(resSource, RDF.type, Core.source)
        .add(resSource, RDFS.label, row.getLiteral(VAR_SRC_LABEL.toString()));
    // Interpretation
    Resource resInterpretation = row.getResource(VAR_INT.toString());
    model.add(main, Core.hasInterpretation, resInterpretation)
        .add(resInterpretation, RDF.type, Core.event)
        .add(resInterpretation, RDFS.label, row.getLiteral(VAR_INT_LABEL.toString()));
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #author, #source}")
  public String findAll(QueryParameters params, Lang lang, Optional<Resource> author,
      Optional<Resource> source) {
    HydraCollectionBuilder builder =
        hydraBuilderFactory.collectionBuilder(ENDPOINT_NAME, Core.act, Api.actOrderByVar, params);
    ExprFactory ef = builder.ef;

    // Add author query
    builder.mapper
        .add("author", Api.actAuthorVar, author);
    author.ifPresent(res -> {
      builder.coreData
          .addWhere(VAR_MAIN, Core.isAuthoredBy, VAR_AUTHOR)
          .addFilter(ef.sameTerm(VAR_AUTHOR, res));
    });

    // Add source query
    builder.mapper
        .add("source", Api.actSourceVar, source);
    source.ifPresent(res -> {
      Path path = PathFactory.pathSeq(PathFactory.pathLink(Core.hasSourceLocation.asNode()),
          PathFactory.pathLink(Core.hasSource.asNode()));
      builder.coreData
          .addWhere(VAR_MAIN, path, VAR_SRC)
          .addFilter(ef.sameTerm(VAR_SRC, res));
    });

    addData(builder.extendedData, false);
    return builder.query(ROW_MAPPER, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder = hydraBuilderFactory.singleBuilder(ENDPOINT_NAME, id, Core.act);
    addData(builder.coreData, true);
    return builder.query(ROW_MAPPER, lang);
  }

  private void addData(WhereBuilder builder, boolean withTypes) {
    ExprFactory ef = builder.getExprFactory();
    builder
        .addWhere(VAR_MAIN, Core.isAuthoredBy, VAR_AUTHOR)
        .addWhere(VAR_AUTHOR, RDFS.label, VAR_AUTHOR_LABEL)
        .addWhere(VAR_MAIN, Core.hasInterpretation, VAR_INT)
        .addWhere(VAR_INT, RDFS.label, VAR_INT_LABEL)
        .addWhere(VAR_MAIN, Core.hasSourceLocation, VAR_LOC)
        .addWhere(VAR_LOC, Core.hasValue, VAR_LOC_TEXT)
        .addWhere(VAR_LOC, Core.hasSource, VAR_SRC)
        .addWhere(VAR_SRC, RDFS.label, VAR_SRC_LABEL)
        .addWhere(VAR_MAIN, Core.isAuthoredOn, VAR_DATE)
        .addWhere(VAR_DATE, Core.hasDateTime, VAR_DATE_TIME);
    if (withTypes) {
      builder
          .addWhere(VAR_LOC, RDF.type, VAR_LOC_TYPE)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOC_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOC_TYPE), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOC_TYPE), RDF.getURI())))
          .addWhere(VAR_LOC, VAR_LOC_TEXT_TYPE, VAR_LOC_TEXT)
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOC_TEXT_TYPE), OWL.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOC_TEXT_TYPE), RDFS.getURI())))
          .addFilter(ef.not(ef.strstarts(ef.str(VAR_LOC_TEXT_TYPE), RDF.getURI())))
          .addFilter(ef.not(ef.sameTerm(VAR_LOC_TEXT_TYPE, Core.hasValue)));
    }
  }

  public UUID insert(Lang lang, List<Resource> authors, Resource source, Literal sourceLocation,
      Resource event) {
    HydraInsertBuilder builder = hydraBuilderFactory.insertBuilder(lang, ENDPOINT_NAME, Core.act,
        DEFAULT_LABEL, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    // Insert authors
    authors.forEach(author -> {
      builder
          .validateType(Core.author, author);
      builder
          .addInsert(builder.root, Core.isAuthoredBy, author);
    });
    // Insert source location
    builder
        .validateType(Core.source, source);
    Resource sourceLocationResource = ResourceFactory.createResource();
    builder
        .addInsert(builder.root, Core.hasSourceLocation, sourceLocationResource)
        .addInsert(sourceLocationResource, RDF.type, Core.sourceLocation)
        .addInsert(sourceLocationResource, Core.hasText, sourceLocation)
        .addInsert(sourceLocationResource, Core.hasSource, source);
    builder
        .addInsert(builder.root, Core.hasSource, source).addInsert(builder.root,
            Core.hasSourceLocation, sourceLocation);
    // Insert event
    builder
        .validateType(Core.event, event);
    builder
        .addInsert(builder.root, Core.hasInterpretation, event);
    // Insert date
    Resource date = ResourceFactory.createResource();
    builder
        .addInsert(builder.root, Core.isAuthoredOn, date)
        .addInsert(date, RDF.type, Core.date)
        .addInsert(date, Core.hasDateTime, ResourceFactory
            .createTypedLiteral(LocalDateTime.now().toString(), XSDDatatype.XSDdateTime));
    // build
    builder.build();
    return builder.id;
  }
}
