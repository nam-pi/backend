package eu.nampi.backend.repository;

import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_MAIN;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_TYPE;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.exception.DeletionNotPermittedException;
import eu.nampi.backend.model.InsertResult;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.queryBuilder.HydraBuilderFactory;
import eu.nampi.backend.queryBuilder.HydraCollectionBuilder;
import eu.nampi.backend.queryBuilder.HydraDeleteBuilder;
import eu.nampi.backend.queryBuilder.HydraInsertBuilder;
import eu.nampi.backend.queryBuilder.HydraSingleBuilder;
import eu.nampi.backend.queryBuilder.HydraUpdateBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "sources")
public class SourceRepository {

  @Autowired
  HydraBuilderFactory hydraBuilderFactory;

  private static final String ENDPOINT_NAME = "sources";
  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    Optional
        .ofNullable(row.getResource(VAR_TYPE.toString()))
        .ifPresentOrElse(type -> model.add(main, RDF.type, type),
            () -> model.add(main, RDF.type, Core.source));
    // Label
    Optional
        .ofNullable(row.getLiteral(VAR_LABEL.toString()))
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional
        .ofNullable(row.getLiteral(VAR_COMMENT.toString()))
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // SameAs
    Optional
        .ofNullable(row.getResource(VAR_SAME_AS.toString()))
        .ifPresent(iri -> model.add(main, Core.sameAs, iri));
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text}")
  public String findAll(QueryParameters params, Lang lang) {
    HydraCollectionBuilder builder = hydraBuilderFactory.collectionBuilder(ENDPOINT_NAME,
        Core.source, Api.sourceOrderByVar, params);
    builder.extendedData.addOptional(VAR_MAIN, Core.sameAs, VAR_SAME_AS);
    return builder.query(ROW_MAPPER, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder = hydraBuilderFactory.singleBuilder(ENDPOINT_NAME, id, Core.source);
    builder.coreData.addOptional(VAR_MAIN, Core.sameAs, VAR_SAME_AS);
    return builder.query(ROW_MAPPER, lang);
  }

  public InsertResult insert(Lang lang, List<Resource> types, List<Literal> labels,
      List<Literal> comments, List<Resource> sameAs) {
    HydraInsertBuilder builder = hydraBuilderFactory.insertBuilder(lang, ENDPOINT_NAME, types,
        labels, comments, new ArrayList<>(), sameAs);
    builder.validateSubresources(Core.source, types);
    builder.build();
    return new InsertResult(builder.root, findOne(lang, builder.id));
  }

  public String update(Lang lang, UUID id, List<Resource> types, List<Literal> labels,
      List<Literal> comments, List<Resource> sameAs) {
    HydraUpdateBuilder builder = hydraBuilderFactory.updateBuilder(lang, id, ENDPOINT_NAME, types,
        labels, comments, new ArrayList<>(), sameAs);
    builder.validateSubresources(Core.source, types);
    builder.build();
    return findOne(lang, builder.id);
  }

  public void delete(UUID id) {
    HydraDeleteBuilder builder = hydraBuilderFactory.deleteBuilder(id, ENDPOINT_NAME, Core.source);
    if (builder.ask(new AskBuilder().addWhere("?sourceLocation", Core.hasSource, builder.root))) {
      throw new DeletionNotPermittedException("The source to be deleted is still in use");
    }
    builder.build();
  }
}
