package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.sparql.HydraCollectionBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "status")
public class StatusRepository extends AbstractHydraRepository {

  public Model findAll(QueryParameters params) {
    // @formatter:off
    HydraCollectionBuilder hydra = new HydraCollectionBuilder(params, Core.status, Api.statusOrderByVar);
    // @formatter:on
    return construct(hydra);
  }

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text}")
  public String findAll(QueryParameters params, Lang lang) {
    Model model = findAll(params);
    return serialize(model, lang);
  }
}
