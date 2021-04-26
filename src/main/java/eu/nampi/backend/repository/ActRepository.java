package eu.nampi.backend.repository;

import java.util.Optional;
import java.util.UUID;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.AbstractHydraBuilder;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;

@Repository
@CacheConfig(cacheNames = "acts")
public class ActRepository extends AbstractHydraRepository {

  public Model findAll(QueryParameters params, Optional<String> author, Optional<String> source) {
    // @formatter:off
    HydraCollectionBuilder hydra = new HydraCollectionBuilder(params, Core.act, Doc.actOrderByVar);
    author.ifPresentOrElse(a -> hydra
        .addMainWhere(Core.isAuthoredBy, "<" + a + ">")
        .addSearchVariable("author", Doc.actAuthorVar, false, "'" + a + "'")
      , () -> hydra
        .addSearchVariable("author", Doc.actAuthorVar, false));
    source.ifPresentOrElse(s -> hydra
        .addMainWhere(PathFactory.pathSeq(PathFactory.pathLink(Core.hasSourceLocation.asNode()), PathFactory.pathLink(Core.hasSource.asNode())), "<" + s + ">")
        .addSearchVariable("source", Doc.actAuthorVar, false, "'" + s + "'")
      , () -> hydra
        .addSearchVariable("source", Doc.actAuthorVar, false));
    // @formatter:on
    addData(hydra);
    return construct(hydra);
  }

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #author, #source}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> author,
      Optional<String> source) {
    Model model = findAll(params, author, source);
    return serialize(model, lang, ResourceFactory.createResource(params.getBaseUrl()));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    String uri = individualsUri(Core.act, id);
    HydraSingleBuilder builder = new HydraSingleBuilder(uri, Core.act);
    addData(builder);
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(uri));
  }

  private void addData(AbstractHydraBuilder<?> builder) {
    // @formatter:off
    builder
      // Author related
        .addMainWhere(Core.isAuthoredBy, "?a")
        .addWhere("?a", RDFS.label, "?al")
        .addMainConstruct(Core.isAuthoredBy, "?a")
        .addConstruct("?a", RDF.type, Core.author)
        .addConstruct("?a", RDFS.label, "?al")
      // Interpretation related
        .addMainWhere(Core.hasInterpretation, "?i")
        .addWhere("?i", RDFS.label, "?il")
        .addMainConstruct(Core.hasInterpretation, "?i")
        .addConstruct("?i", RDF.type, Core.event)
        .addConstruct("?i", RDFS.label, "?il")
      // Source location related
        .addMainWhere(Core.hasSourceLocation, "?sl")
        .addWhere("?sl", Core.hasXsdString, "?sls")
        .addWhere("?sl", Core.hasSource, "?src")
        .addWhere("?src", RDFS.label, "?srcl")
        .addMainConstruct(Core.hasSourceLocation, "?sl")
        .addConstruct("?sl", RDF.type, Core.sourceLocation)
        .addConstruct("?sl", Core.hasXsdString, "?sls")
        .addConstruct("?sl", Core.hasSource, "?src")
        .addConstruct("?src", RDF.type, Core.source)
        .addConstruct("?src", RDFS.label, "?srcl")
      // Authoring date related
        .addMainWhere(Core.isAuthoredOn, "?ad")
        .addWhere("?ad", Core.hasXsdDateTime, "?adt")
        .addMainConstruct(Core.isAuthoredOn, "?ad")
        .addConstruct("?ad", Core.hasXsdDateTime, "?adt")
        .addConstruct("?ad", RDF.type, Core.date);
    // @formatter:on
  }

}
