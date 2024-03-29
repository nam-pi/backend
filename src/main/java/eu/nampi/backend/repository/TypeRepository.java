package eu.nampi.backend.repository;

import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_MAIN;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.queryBuilder.HydraBuilderFactory;
import eu.nampi.backend.queryBuilder.HydraCollectionBuilder;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.util.Serializer;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Hydra;

@Repository
@CacheConfig(cacheNames = "types")
public class TypeRepository {

  @Autowired
  Serializer serializer;

  @Autowired
  HydraBuilderFactory hydraBuilderFactory;

  @Autowired
  JenaService jenaService;

  @Value("${nampi.crm-prefix}")
  String crmPrefix;

  private static final String ENDPOINT_NAME = "types";

  Pattern totalItemsRegex = Pattern.compile("\"totalItems\":\"(\\d*)\"");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    model.add(main, RDF.type, RDFS.Resource);
    // Label
    Optional
        .ofNullable(row.getLiteral(VAR_LABEL.toString()))
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional
        .ofNullable(row.getLiteral(VAR_COMMENT.toString()))
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    return main;
  };

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type}")
  public String findAll(QueryParameters params, Lang lang) {
    // Try to get results as class
    HydraCollectionBuilder classesBuilder = hydraBuilderFactory.collectionBuilder(ENDPOINT_NAME,
        RDFS.Resource, Api.typeOrderByProp, params, false, false);
    var ef = classesBuilder.ef;
    classesBuilder.coreData
        .addWhere(VAR_MAIN, RDFS.subClassOf, params.getType().orElseThrow())
        .addFilter(ef.not(ef.strstarts(ef.str(VAR_MAIN), crmPrefix)));
    classesBuilder.build(ROW_MAPPER);
    StmtIterator iterator =
        classesBuilder.model.listStatements(classesBuilder.root, Hydra.totalItems, (RDFNode) null);
    while (iterator.hasNext()) {
      int totalItems = (Integer) iterator.next().asTriple().getObject().getLiteral().getValue();
      if (totalItems > 0) {
        return serializer.serialize(classesBuilder.model, lang, classesBuilder.root);
      }
    }
    // Try to get results as property
    HydraCollectionBuilder builder =
        hydraBuilderFactory.collectionBuilder(ENDPOINT_NAME, RDFS.Resource,
            Api.typeOrderByProp, params, false, false);
    builder.coreData.addWhere(VAR_MAIN, RDFS.subPropertyOf,
        params.getType().orElseThrow());
    return builder.query(ROW_MAPPER, lang);
  }

  @Cacheable(key = "{#type, #node}")
  public boolean isType(RDFNode type, RDFNode node) {
    if (type.toString().equals(node.toString())) {
      return true;
    }
    AskBuilder builder = new AskBuilder().addWhere(node, RDF.type, type);
    return jenaService.ask(builder);
  }
}
