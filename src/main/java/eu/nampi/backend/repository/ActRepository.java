package eu.nampi.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;

@Repository
@CacheConfig(cacheNames = "acts")
public class ActRepository extends AbstractHydraRepository {

  private static final Node VAR_AUTHOR = NodeFactory.createVariable("author");
  private static final Node VAR_AUTHOR_LABEL = NodeFactory.createVariable("authorLabel");
  private static final Node VAR_AUTHORED_DATE = NodeFactory.createVariable("authoredDate");
  private static final Node VAR_AUTHORED_DATE_TIME = NodeFactory.createVariable("authoredDateTime");
  private static final Node VAR_INTERPRETATION = NodeFactory.createVariable("interpretation");
  private static final Node VAR_INTERPRETATION_LABEL = NodeFactory.createVariable("interpretationLabel");
  private static final Node VAR_SOURCE_LOCATION = NodeFactory.createVariable("sourceLocation");
  private static final Node VAR_SOURCE_LOCATION_SOURCE = NodeFactory.createVariable("sourceLocationSource");
  private static final Node VAR_SOURCE_LOCATION_SOURCE_LABEL = NodeFactory.createVariable("sourceLocationSourceLabel");
  private static final Node VAR_SOURCE_LOCATION_STRING = NodeFactory.createVariable("sourceLocationString");

  public Model findAll(QueryParameters params, Optional<String> author, Optional<String> source) {
    HydraCollectionBuilder builder = new HydraCollectionBuilder(endpointUri("acts"), Core.act, Doc.actOrderByVar,
        params);
    ExprFactory ef = builder.getExprFactory();
    Node varMain = HydraCollectionBuilder.VAR_MAIN;

    // @formatter:off
    if(author.isPresent()) {
      Node varAuthor = NodeFactory.createVariable("filterAuthor");
      builder.dataWhere
        .addWhere(varMain, Core.isAuthoredBy, varAuthor)
        .addFilter(ef.sameTerm(varAuthor, ResourceFactory.createResource(author.get())));
      builder.countWhere
        .addWhere(varMain, Core.isAuthoredBy, varAuthor)
        .addFilter(ef.sameTerm(varAuthor, ResourceFactory.createResource(author.get())));
    }

    if(source.isPresent()) {
      Node varSource = NodeFactory.createVariable("filterSource");
      Path path = PathFactory.pathSeq(PathFactory.pathLink(Core.hasSourceLocation.asNode()), PathFactory.pathLink(Core.hasSource.asNode()));
      builder.dataWhere
        .addWhere(varMain, path, varSource)
        .addFilter(ef.sameTerm(varSource, ResourceFactory.createResource(source.get())));
      builder.countWhere
        .addWhere(varMain, path, varSource)
        .addFilter(ef.sameTerm(varSource, ResourceFactory.createResource(source.get())));
    }

    builder.dataWhere.addWhere(dataWhere(varMain)).addWhere(builder.commentWhere());
    addData(builder, varMain);

    builder.mapper
      .add("author", Doc.actAuthorVar, author.orElse(""))
      .add("source", Doc.actSourceVar, source.orElse(""));

    // @formatter:on
    return construct(builder);
  }

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #author, #source}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> author, Optional<String> source) {
    Model model = findAll(params, author, source);
    return serialize(model, lang, ResourceFactory.createResource(params.getBaseUrl()));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder = new HydraSingleBuilder(individualsUri(Core.act, id), Core.act);
    builder.addWhere(dataWhere(HydraSingleBuilder.VAR_MAIN));
    addData(builder, HydraSingleBuilder.VAR_MAIN);
    builder.addWhere(builder.commentWhere());
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(builder.iri));
  }

  private WhereBuilder dataWhere(Node varMain) {
    // @formatter:off
    return new WhereBuilder()
        .addWhere(varMain, Core.isAuthoredBy, VAR_AUTHOR)
        .addWhere(VAR_AUTHOR, RDFS.label, VAR_AUTHOR_LABEL)
        
        .addWhere(varMain, Core.hasInterpretation, VAR_INTERPRETATION)
        .addWhere(VAR_INTERPRETATION, RDFS.label, VAR_INTERPRETATION_LABEL)

        .addWhere(varMain, Core.hasSourceLocation, VAR_SOURCE_LOCATION)
        .addWhere(VAR_SOURCE_LOCATION, Core.hasXsdString, VAR_SOURCE_LOCATION_STRING)
        .addWhere(VAR_SOURCE_LOCATION, Core.hasSource, VAR_SOURCE_LOCATION_SOURCE)
        .addWhere(VAR_SOURCE_LOCATION_SOURCE, RDFS.label, VAR_SOURCE_LOCATION_SOURCE_LABEL)

        .addWhere(varMain, Core.isAuthoredOn, VAR_AUTHORED_DATE)
        .addWhere(VAR_AUTHORED_DATE, Core.hasXsdDateTime, VAR_AUTHORED_DATE_TIME);
    // @formatter:on
  }

  private void addData(ConstructBuilder builder, Node varMain) {
    // @formatter:off
    builder
        .addConstruct(varMain, Core.isAuthoredBy, VAR_AUTHOR)
        .addConstruct(VAR_AUTHOR, RDF.type, Core.author)
        .addConstruct(VAR_AUTHOR, RDFS.label, VAR_AUTHOR_LABEL)

        .addConstruct(varMain, Core.hasInterpretation, VAR_INTERPRETATION)
        .addConstruct(VAR_INTERPRETATION, RDF.type, Core.event)
        .addConstruct(VAR_INTERPRETATION, RDFS.label, VAR_INTERPRETATION_LABEL)

        .addConstruct(varMain, Core.hasSourceLocation, VAR_SOURCE_LOCATION)
        .addConstruct(VAR_SOURCE_LOCATION, RDF.type, Core.sourceLocation)
        .addConstruct(VAR_SOURCE_LOCATION, Core.hasXsdString, VAR_SOURCE_LOCATION_STRING)
        .addConstruct(VAR_SOURCE_LOCATION, Core.hasSource, VAR_SOURCE_LOCATION_SOURCE)
        .addConstruct(VAR_SOURCE_LOCATION_SOURCE, RDF.type, Core.source)
        .addConstruct(VAR_SOURCE_LOCATION_SOURCE, RDFS.label, VAR_SOURCE_LOCATION_SOURCE_LABEL)

        .addConstruct(varMain, Core.isAuthoredOn, VAR_AUTHORED_DATE)
        .addConstruct(VAR_AUTHORED_DATE, Core.hasXsdDateTime, VAR_AUTHORED_DATE_TIME)
        .addConstruct(VAR_AUTHORED_DATE, RDF.type, Core.date);
    // @formatter:on
  }
}
