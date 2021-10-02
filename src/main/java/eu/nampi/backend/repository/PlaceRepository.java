package eu.nampi.backend.repository;

import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_MAIN;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_TEXT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_TYPE;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.exception.DeletionNotPermittedException;
import eu.nampi.backend.model.InsertResult;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.queryBuilder.AbstractHydraUpdateBuilder;
import eu.nampi.backend.queryBuilder.HydraBuilderFactory;
import eu.nampi.backend.queryBuilder.HydraCollectionBuilder;
import eu.nampi.backend.queryBuilder.HydraDeleteBuilder;
import eu.nampi.backend.queryBuilder.HydraInsertBuilder;
import eu.nampi.backend.queryBuilder.HydraSingleBuilder;
import eu.nampi.backend.queryBuilder.HydraUpdateBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "places")
public class PlaceRepository {

  @Autowired
  HydraBuilderFactory hydraBuilderFactory;

  private static final String ENDPOINT_NAME = "places";
  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");
  private static final Node VAR_LATITUDE = NodeFactory.createVariable("latitude");
  private static final Node VAR_LONGITUDE = NodeFactory.createVariable("longitude");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    Optional
        .ofNullable(row.getResource(VAR_TYPE.toString()))
        .ifPresentOrElse(type -> model.add(main, RDF.type, type),
            () -> model.add(main, RDF.type, Core.place));
    // Label
    Optional
        .ofNullable(row.getLiteral(VAR_LABEL.toString()))
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional
        .ofNullable(row.getLiteral(VAR_COMMENT.toString()))
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // Text
    Optional
        .ofNullable(row.getLiteral(VAR_TEXT.toString()))
        .ifPresent(text -> model.add(main, Core.hasText, text));
    // SameAs
    Optional
        .ofNullable(row.getResource(VAR_SAME_AS.toString()))
        .ifPresent(iri -> model.add(main, Core.sameAs, iri));
    // Latitude & Longitude
    Optional.ofNullable(row.getLiteral(VAR_LATITUDE.toString())).ifPresent(
        latitude -> Optional.ofNullable(row.getLiteral(VAR_LONGITUDE.toString()))
            .ifPresent(longitude -> model
                .add(main, Core.hasLatitude, latitude)
                .add(main, Core.hasLongitude, longitude)));
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text}")
  public String findAll(QueryParameters params, Lang lang) {
    HydraCollectionBuilder builder = hydraBuilderFactory.collectionBuilder(ENDPOINT_NAME,
        Core.place, Api.placeOrderByVar, params, false);
    ExprFactory ef = builder.ef;
    builder.extendedData
        .addOptional(VAR_MAIN, Core.hasText, VAR_TEXT)
        .addOptional(VAR_MAIN, Core.sameAs, VAR_SAME_AS)
        .addOptional(VAR_MAIN, Core.hasLatitude, VAR_LATITUDE)
        .addOptional(VAR_MAIN, Core.hasLongitude, VAR_LONGITUDE);
    // Add custom text select
    params.getText().ifPresent(text -> {
      Node varSearchString = NodeFactory.createVariable("searchString");
      Path path = PathFactory.pathAlt(PathFactory.pathLink(RDFS.label.asNode()),
          PathFactory.pathLink(Core.hasText.asNode()));
      builder.coreData.addOptional(VAR_MAIN, path, varSearchString)
          .addFilter(ef.regex(varSearchString, params.getText().get(), "i"));
    });
    return builder.query(ROW_MAPPER, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder = hydraBuilderFactory.singleBuilder(ENDPOINT_NAME, id, Core.place);
    builder.coreData
        .addOptional(VAR_MAIN, Core.hasText, VAR_TEXT)
        .addOptional(VAR_MAIN, Core.sameAs, VAR_SAME_AS)
        .addOptional(VAR_MAIN, Core.hasLatitude, VAR_LATITUDE)
        .addOptional(VAR_MAIN, Core.hasLongitude, VAR_LONGITUDE);
    return builder.query(ROW_MAPPER, lang);
  }

  public InsertResult insert(Lang lang, Resource type, List<Literal> labels, List<Literal> comments,
      List<Literal> texts, List<Resource> sameAs, Optional<Double> optionalLatitude,
      Optional<Double> optionalLongitude) {
    HydraInsertBuilder builder = hydraBuilderFactory.insertBuilder(lang, ENDPOINT_NAME, type,
        labels, comments, texts, sameAs);
    builder.validateSubnode(Core.place, type);
    addPlace(builder, optionalLatitude, optionalLongitude);
    builder.build();
    return new InsertResult(builder.root, findOne(lang, builder.id));
  }

  public String update(Lang lang, UUID id, Resource type, List<Literal> labels,
      List<Literal> comments, List<Literal> texts, List<Resource> sameAs,
      Optional<Double> optionalLatitude, Optional<Double> optionalLongitude) {
    HydraUpdateBuilder builder = hydraBuilderFactory.updateBuilder(lang, id, ENDPOINT_NAME, type,
        labels, comments, texts, sameAs);
    builder.validateSubnode(Core.place, type);
    addPlace(builder, optionalLatitude, optionalLongitude);
    builder.build();
    return findOne(lang, builder.id);
  }

  private void addPlace(AbstractHydraUpdateBuilder builder, Optional<Double> optionalLatitude,
      Optional<Double> optionalLongitude) {
    if (optionalLatitude.isPresent() || optionalLongitude.isPresent()) {
      if (optionalLatitude.isEmpty() && optionalLongitude.isPresent()) {
        throw new IllegalArgumentException(
            "Latitude is missing. Either none or both coordinate numbers need to be present");
      }
      if (optionalLatitude.isPresent() && optionalLongitude.isEmpty()) {
        throw new IllegalArgumentException(
            "Longitude is missing. Either none or both coordinate numbers need to be present");
      }
      Double latitude = optionalLatitude.get();
      Double longitude = optionalLongitude.get();
      if (latitude < -90 || latitude > 90) {
        throw new IllegalArgumentException(
            String.format("Latitude '%s' needs to be between -90 and 90", latitude));
      }
      if (longitude < -180 || longitude > 180) {
        throw new IllegalArgumentException(
            String.format("Longitude '%s' needs to be between -180 and 180", longitude));
      }
      builder
          .addInsert(builder.root, Core.hasLatitude, ResourceFactory.createTypedLiteral(latitude))
          .addInsert(builder.root, Core.hasLongitude,
              ResourceFactory.createTypedLiteral(longitude));
    }
  }

  public void delete(UUID id) {
    HydraDeleteBuilder builder = hydraBuilderFactory.deleteBuilder(id, ENDPOINT_NAME, Core.place);
    if (builder.ask(new AskBuilder().addWhere("?event", Core.takesPlaceAt, builder.root))) {
      throw new DeletionNotPermittedException("The place to be deleted is still in use");
    }
    builder.build();
  }
}
