package eu.nampi.backend.repository;

import java.util.UUID;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;
import eu.nampi.backend.vocabulary.SchemaOrg;

@Repository
@CacheConfig(cacheNames = "sources")
public class SourceRepository extends AbstractHydraRepository {

  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");

  public Model findAll(QueryParameters params) {
    HydraCollectionBuilder builder = new HydraCollectionBuilder(endpointUri("sources"), Core.source, Doc.sourceOrderByVar,
        params);
    builder.dataWhere.addOptional(HydraCollectionBuilder.VAR_MAIN, SchemaOrg.sameAs, VAR_SAME_AS);
    addData(builder, HydraCollectionBuilder.VAR_MAIN);
    return construct(builder);
  }

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text}")
  public String findAll(QueryParameters params, Lang lang) {
    Model model = findAll(params);
    return serialize(model, lang, ResourceFactory.createResource(params.getBaseUrl()));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder = new HydraSingleBuilder(individualsUri(Core.source, id), Core.source);
    addData(builder, HydraSingleBuilder.VAR_MAIN);
    builder.addOptional(HydraCollectionBuilder.VAR_MAIN, SchemaOrg.sameAs, VAR_SAME_AS);
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(builder.iri));
  }

  private void addData(ConstructBuilder builder, Node varMain) {
    builder.addConstruct(varMain, SchemaOrg.sameAs, VAR_SAME_AS);
  }
}
