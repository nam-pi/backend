package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_MAIN;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.WhereBuilder;
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
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder;
import eu.nampi.backend.model.hydra.temp.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.temp.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.SchemaOrg;

@Repository
@CacheConfig(cacheNames = "aspects")
public class AspectRepository extends AbstractHydraRepository {

  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");
  private static final Node VAR_STRING = NodeFactory.createVariable("string");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    model.add(main, RDF.type, Core.aspect);
    // Label
    Optional.ofNullable(row.getLiteral(VAR_LABEL.toString())).map(Literal::getString)
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional.ofNullable(row.getLiteral(VAR_COMMENT.toString())).map(Literal::getString)
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // XSD-String
    Optional.ofNullable(row.getLiteral(VAR_STRING.toString())).map(Literal::getString)
        .ifPresent(string -> model.add(main, Core.hasXsdString, string));
    // SameAs
    Optional.ofNullable(row.getResource(VAR_SAME_AS.toString())).map(Resource::getURI)
        .ifPresent(string -> model.add(main, SchemaOrg.sameAs, string));
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #participant}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> participant) {
    HydraCollectionBuilder builder = new HydraCollectionBuilder(jenaService, endpointUri("aspects"),
        Core.aspect, Api.aspectOrderByVar, params, false);
    ExprFactory ef = builder.ef;

    // Add participant query
    builder.mapper.add("participant", Api.aspectParticipantVar, participant);
    participant.map(ResourceFactory::createResource).ifPresent(resParticipant -> {
      Path path = PathFactory.pathSeq(
          PathFactory.pathLink(Core.aspectIsUsedIn.asNode()),
          PathFactory.pathLink(Core.hasParticipant.asNode()));
      builder.coreData.addWhere(VAR_MAIN, path, resParticipant);
    });

    // Add custom text select
    params.getText().ifPresent(text -> {
      Node varSearchString = NodeFactory.createVariable("searchString");
      Path path = PathFactory.pathAlt(
          PathFactory.pathLink(RDFS.label.asNode()),
          PathFactory.pathLink(Core.hasXsdString.asNode()));
      builder.coreData
          .addOptional(VAR_MAIN, path, varSearchString)
          .addFilter(ef.regex(varSearchString, params.getText().get(), "i"));
    });

    addData(builder.extendedData);
    return build(builder, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder =
        new HydraSingleBuilder(jenaService, individualsUri(Core.aspect, id), Core.aspect);
    addData(builder.coreData);
    return build(builder, lang);
  }

  private String build(AbstractHydraBuilder builder, Lang lang) {
    builder.build(ROW_MAPPER);
    return serialize(builder.model, lang, builder.root);
  }

  private void addData(WhereBuilder builder) {
    builder
        .addOptional(VAR_MAIN, Core.hasXsdString, VAR_STRING)
        .addOptional(VAR_MAIN, SchemaOrg.sameAs, VAR_SAME_AS);
  }

}
