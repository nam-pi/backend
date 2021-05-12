package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_MAIN;
import java.util.Optional;
import java.util.function.BiFunction;
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
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.temp.HydraCollectionBuilder;
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
        RDFS.Class, Api.classOrderByVar, params, true, true);

    // Add data
    builder.dataSelect
        .addOptional(VAR_MAIN, RDFS.comment, VAR_COMMENT);

    // Add ancestor query
    builder.mapper.add("ancestor", RDFS.Class, ancestor);
    ancestor.map(ResourceFactory::createResource).ifPresent(res -> {
      Expr filter = builder.ef.sameTerm(VAR_ANCESTOR, res);
      WhereBuilder where = new WhereBuilder()
          .addWhere(VAR_MAIN, RDFS.subClassOf, VAR_ANCESTOR)
          .addFilter(filter);
      builder.dataSelect.addWhere(where);
      builder.countWhere.addWhere(where);
    });

    builder.build(ROW_MAPPER);
    return serialize(builder.model, lang, builder.root);
  }

}
