package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_MAIN;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.Author;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder;
import eu.nampi.backend.model.hydra.temp.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.temp.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@CacheConfig(cacheNames = "authors")
public class AuthorRepository extends AbstractHydraRepository {

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    model.add(main, RDF.type, Core.author);
    // Label
    model.add(main, RDFS.label, row.getLiteral(VAR_LABEL.toString()).getString());
    return main;
  };


  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text}")
  public String findAll(QueryParameters params, Lang lang) {
    HydraCollectionBuilder builder = new HydraCollectionBuilder(jenaService, endpointUri("authors"),
        Core.author, Api.authorOrderByVar, params, false, false);
    return build(builder, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder =
        new HydraSingleBuilder(jenaService, individualsUri(Core.author, id), Core.author);
    return build(builder, lang);
  }

  public Optional<Author> findOne(UUID rdfId) {
    AtomicReference<Optional<Author>> authorRef = new AtomicReference<>(Optional.empty());
    SelectBuilder selectBuilder = new SelectBuilder();
    String iri = individualsUri(Core.author, rdfId);
    try {
      selectBuilder.addValueVar("?l").addWhere("?a", RDF.type, Core.author)
          .addWhere("?a", RDFS.label, "?l").addFilter("?a = <" + iri + ">");
    } catch (ParseException e) {
      log.warn(e.getMessage());
    }
    jenaService.select(selectBuilder, (qs) -> {
      String label = qs.getLiteral("?l").getString();
      authorRef.set(Optional.of(new Author(iri, rdfId, label)));
    });
    return authorRef.get();
  }

  public Author addOne(UUID rdfId, String label) {
    String iri = individualsUri(Core.author, rdfId);
    Resource authorRes = ResourceFactory.createResource(iri);
    UpdateBuilder updateBuilder = new UpdateBuilder().addInsert(authorRes, RDF.type, Core.author)
        .addInsert(authorRes, RDFS.label, label);
    jenaService.update(updateBuilder);
    return new Author(iri, rdfId, label);
  }

  public Author updateLabel(Author author, String newLabel) {
    UpdateBuilder updateBuilder = new UpdateBuilder();
    try {
      updateBuilder.addDelete("?a", RDFS.label, "?l").addInsert("?a", RDFS.label, newLabel)
          .addWhere(new WhereBuilder().addWhere("?a", RDF.type, Core.author)
              .addWhere("?a", RDFS.label, "?l").addFilter("?a = <" + author.getIri() + ">"));
    } catch (ParseException e) {
      log.error(e.getMessage());
    }
    author.setLabel(newLabel);
    jenaService.update(updateBuilder);
    return author;
  }

  private String build(AbstractHydraBuilder builder, Lang lang) {
    builder.build(ROW_MAPPER);
    return serialize(builder.model, lang, builder.root);
  }

}
