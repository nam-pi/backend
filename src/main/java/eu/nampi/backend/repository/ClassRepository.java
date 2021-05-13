package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.VAR_MAIN;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.vocabulary.Api;

@Repository
@CacheConfig(cacheNames = "classes")
public class ClassRepository extends AbstractHydraRepository {

  private static final Node VAR_ANCESTOR = NodeFactory.createVariable("ancestor");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    model.add(main, RDF.type, RDFS.Class);
    // Label
    Optional.ofNullable(row.getLiteral(VAR_LABEL.toString())).map(Literal::getString)
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional.ofNullable(row.getLiteral(VAR_COMMENT.toString())).map(Literal::getString)
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));;
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #ancestor}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> ancestor) {

    HydraCollectionBuilder builder = new HydraCollectionBuilder(jenaService, endpointUri("classes"),
        RDFS.Class, Api.classOrderByVar, params);

    // Add ancestor query
    builder.mapper.add("ancestor", RDFS.Class, ancestor);
    ancestor.map(ResourceFactory::createResource).ifPresent(res -> {
      builder.coreData
          .addWhere(VAR_MAIN, RDFS.subClassOf, VAR_ANCESTOR)
          .addFilter(builder.ef.sameTerm(VAR_ANCESTOR, res));
    });

    builder.extendedData.addWhere(VAR_MAIN, RDFS.subClassOf, VAR_ANCESTOR);

    builder.build(ROW_MAPPER);
    return serialize(builder.model, lang, builder.root);
  }

}
