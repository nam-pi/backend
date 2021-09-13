package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_MAIN;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.utils.HydraUtils;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Hydra;

@Repository
@CacheConfig(cacheNames = "types")
public class TypeRepository extends AbstractHydraRepository {

  private static final String ENDPOINT_NAME = "types";
  Pattern totalItemsRegex = Pattern.compile("\"totalItems\":\"(\\d*)\"");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    model.add(main, RDF.type, RDFS.Resource);
    // Label
    Optional.ofNullable(row.getLiteral(VAR_LABEL.toString())).ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional.ofNullable(row.getLiteral(VAR_COMMENT.toString()))
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    ;
    return main;
  };

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type}")
  public String findAll(QueryParameters params, Lang lang) {
    // Try to get results as class
    HydraCollectionBuilder classesBuilder = new HydraCollectionBuilder(jenaService, endpointUri(ENDPOINT_NAME),
        RDFS.Resource, Api.typeOrderByVar, params, false, false);
    classesBuilder.coreData.addWhere(VAR_MAIN, RDFS.subClassOf, params.getType().orElseThrow());
    classesBuilder.build(ROW_MAPPER);
    StmtIterator iterator = classesBuilder.model.listStatements(classesBuilder.root, Hydra.totalItems, (RDFNode) null);
    while (iterator.hasNext()) {
      int totalItems = (Integer) iterator.next().asTriple().getObject().getLiteral().getValue();
      if (totalItems > 0) {
        return HydraUtils.serialize(classesBuilder.model, lang, classesBuilder.root);
      }
    }
    // Try to get results as property
    HydraCollectionBuilder propertiesBuilder = new HydraCollectionBuilder(jenaService, endpointUri("types"),
        RDFS.Resource, Api.typeOrderByVar, params, false, false);
    propertiesBuilder.coreData.addWhere(VAR_MAIN, RDFS.subPropertyOf, params.getType().orElseThrow());
    propertiesBuilder.build(ROW_MAPPER);
    return HydraUtils.serialize(propertiesBuilder.model, lang, propertiesBuilder.root);
  }

}
