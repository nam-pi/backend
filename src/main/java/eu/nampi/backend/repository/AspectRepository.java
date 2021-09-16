package eu.nampi.backend.repository;

import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_MAIN;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_TYPE;
import java.util.List;
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
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.queryBuilder.HydraCollectionBuilder;
import eu.nampi.backend.queryBuilder.HydraDeleteBuilder;
import eu.nampi.backend.queryBuilder.HydraInsertBuilder;
import eu.nampi.backend.queryBuilder.HydraSingleBuilder;
import eu.nampi.backend.queryBuilder.HydraUpdateBuilder;
import eu.nampi.backend.utils.HydraBuilderFactory;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "aspects")
public class AspectRepository {

  private static final String ENDPOINT_NAME = "aspects";
  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");
  private static final Node VAR_STRING = NodeFactory.createVariable("string");

  @Autowired
  HydraBuilderFactory hydraBuilderFactory;

  @Autowired
  HierarchyRepository hierarchyRepository;

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    Optional.ofNullable(row.getResource(VAR_TYPE.toString())).ifPresentOrElse(type -> {
      model.add(main, RDF.type, type);
    }, () -> {
      model.add(main, RDF.type, Core.aspect);
    });
    // Label
    Optional
        .ofNullable(row.getLiteral(VAR_LABEL.toString()))
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional
        .ofNullable(row.getLiteral(VAR_COMMENT.toString()))
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // Text
    Optional
        .ofNullable(row.getLiteral(VAR_STRING.toString()))
        .ifPresent(text -> model.add(main, Core.hasText, text));
    // SameAs
    Optional
        .ofNullable(row.getResource(VAR_SAME_AS.toString()))
        .ifPresent(iri -> model.add(main, Core.sameAs, iri));
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #participant}")
  public String findAll(QueryParameters params, Lang lang, Optional<Resource> participant) {
    HydraCollectionBuilder builder = hydraBuilderFactory.collectionBuilder(ENDPOINT_NAME,
        Core.aspect, Api.aspectOrderByVar, params, false);
    ExprFactory ef = builder.ef;

    // Add participant query
    builder.mapper.add("participant", Api.aspectParticipantVar, participant);
    participant.ifPresent(resParticipant -> {
      Path path = PathFactory.pathSeq(PathFactory.pathLink(Core.aspectIsUsedIn.asNode()),
          PathFactory.pathLink(Core.hasParticipant.asNode()));
      builder.coreData.addWhere(VAR_MAIN, path, resParticipant);
    });

    // Add custom text select
    params.getText().ifPresent(text -> {
      Node varSearchString = NodeFactory.createVariable("searchString");
      Path path = PathFactory.pathAlt(PathFactory.pathLink(RDFS.label.asNode()),
          PathFactory.pathLink(Core.hasText.asNode()));
      builder.coreData.addOptional(VAR_MAIN, path, varSearchString)
          .addFilter(ef.regex(varSearchString, params.getText().get(), "i"));
    });

    addData(builder.extendedData);
    return builder.query(ROW_MAPPER, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder =
        hydraBuilderFactory.singleBuilder(ENDPOINT_NAME, id, Core.aspect);
    addData(builder.coreData);
    return builder.query(ROW_MAPPER, lang);
  }

  private void addData(WhereBuilder builder) {
    builder
        .addOptional(VAR_MAIN, Core.hasText, VAR_STRING)
        .addOptional(VAR_MAIN, Core.sameAs, VAR_SAME_AS);
  }

  public String insert(Lang lang, Resource type, List<Literal> labels, List<Literal> comments,
      List<Literal> texts) {
    HydraInsertBuilder builder =
        hydraBuilderFactory.insertBuilder(lang, ENDPOINT_NAME, type, labels, comments, texts);
    builder.validateSubtype(Core.aspect, type);
    builder.build();
    return findOne(lang, builder.id);
  }

  public String update(Lang lang, UUID id, Resource type, List<Literal> labels,
      List<Literal> comments, List<Literal> texts) {
    HydraUpdateBuilder builder = hydraBuilderFactory.updateBuilder(lang, id, ENDPOINT_NAME, type,
        labels, comments, texts);
    builder.validateSubtype(Core.aspect, type);
    builder.build();
    return findOne(lang, builder.id);
  }

  public void delete(UUID id) {
    HydraDeleteBuilder builder = hydraBuilderFactory.deleteBuilder(id, ENDPOINT_NAME, Core.aspect);
    builder.build();
  }
}
