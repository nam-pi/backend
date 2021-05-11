package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_MAIN;
import java.util.Optional;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.temp.HydraCollectionBuilder;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.vocabulary.Api;



@Repository
@CacheConfig(cacheNames = "classes")
public class ClassRepository extends AbstractHydraRepository {

  public static final Node VAR_PARENT = NodeFactory.createVariable("ancestor");

  @Autowired
  private JenaService jenaService;

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #ancestor}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> ancestor) {

    HydraCollectionBuilder builder = new HydraCollectionBuilder(jenaService, endpointUri("classes"),
        RDFS.Class, Api.classOrderByVar, params);

    if (ancestor.isPresent()) {
      Resource resParent = ResourceFactory.createResource(ancestor.get());
      Expr filter = builder.ef.sameTerm(VAR_PARENT, resParent);
      WhereBuilder where = new WhereBuilder()
          .addWhere(VAR_MAIN, RDFS.subClassOf, VAR_PARENT)
          .addFilter(filter);
      builder.dataSelect.addWhere(where);
      builder.countWhere.addWhere(where);
    }

    builder.dataSelect.addOptional(VAR_MAIN, RDFS.comment, VAR_COMMENT);

    builder.mapper.add("ancestor", RDFS.Class, ancestor);

    builder.build((model, row) -> {
      Resource base = row.getResource(VAR_MAIN.toString());
      String label = row.getLiteral(VAR_LABEL.toString()).getString();
      model
          .add(base, RDF.type, RDFS.Class)
          .add(base, RDFS.label, label);
      return base;
    });

    return serialize(builder.model, lang, builder.root);
  }

}
