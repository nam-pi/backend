package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.HydraBuilder;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "persons")
public class PersonRepository extends AbstractHydraRepository {

  public Model findAll(QueryParameters params) {
    // @formatter:off
    HydraBuilder hydra = new HydraBuilder(params, Core.person, Api.orderBy);
    // @formatter:on
    return construct(hydra);
  }

  @Cacheable(key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type}")
  public String findAll(QueryParameters params, Lang lang) {
    Model model = findAll(params);
    return serialize(model, lang);
  }

}
