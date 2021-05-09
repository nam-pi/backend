package eu.nampi.backend.repository;

import java.util.Optional;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.expr.Expr;
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
  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #parent}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> parent) {
    HydraCollectionBuilder builder = new HydraCollectionBuilder(endpointUri("classes"), RDFS.Class,
        Api.classOrderByVar, params, true, true);
    ExprFactory ef = builder.ef;

    if (parent.isPresent()) {
      Node varParent = NodeFactory.createVariable("parent");
      Resource parentResource = ResourceFactory.createResource(parent.get());
      Expr filter = ef.sameTerm(varParent, parentResource);
      builder.dataWhere.addWhere(HydraCollectionBuilder.VAR_MAIN, RDFS.subClassOf, varParent)
          .addFilter(filter);
      builder.countWhere.addWhere(HydraCollectionBuilder.VAR_MAIN, RDFS.subClassOf, varParent)
          .addFilter(filter);
    }
    builder.dataWhere.addWhere(builder.commentWhere());

    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(endpointUri("classes")));

  }
}
