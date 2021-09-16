package eu.nampi.backend.repository;

import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_MAIN;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_TYPE;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.queryBuilder.HydraCollectionBuilder;
import eu.nampi.backend.queryBuilder.HydraSingleBuilder;
import eu.nampi.backend.utils.HydraBuilderFactory;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "persons")
public class PersonRepository {

  @Autowired
  HydraBuilderFactory hydraBuilderFactory;

  private static final String ENDPOINT_NAME = "persons";
  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");
  private static final String PREF_BIRTH = "birth";
  private static final String PREF_DEATH = "death";

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    Optional
        .ofNullable(row.getResource(VAR_TYPE.toString()))
        .ifPresentOrElse(type -> model.add(main, RDF.type, type),
            () -> model.add(main, RDF.type, Core.person));
    // Label
    Optional
        .ofNullable(row.getLiteral(VAR_LABEL.toString()))
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional
        .ofNullable(row.getLiteral(VAR_COMMENT.toString()))
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // SameAs
    Optional
        .ofNullable(row.getResource(VAR_SAME_AS.toString()))
        .ifPresent(iri -> model.add(main, Core.sameAs, iri));
    // Birth
    addEvent(model, row, main, PREF_BIRTH, Core.isBornIn);
    addEvent(model, row, main, PREF_DEATH, Core.diesIn);
    return main;
  };

  private static final void addEvent(Model model, QuerySolution row, Resource main, String base,
      Property type) {
    Optional
        .ofNullable(row.getResource(NodeFactory.createVariable(base).toString()))
        .ifPresent(evt -> {
          model
              .add(main, type, evt)
              .add(evt, RDF.type, Core.event);
          Optional
              .ofNullable(row.getLiteral(varEventLabel(base).toString()))
              .ifPresent(label -> model.add(evt, RDFS.label, label));
          Optional
              .ofNullable(row.getResource(varDateExact(base).toString()))
              .ifPresent(resDate -> model
                  .add(evt, Core.takesPlaceOn, resDate)
                  .add(resDate, RDF.type, Core.date)
                  .add(resDate, Core.hasDateTime,
                      row.getLiteral(varDateTimeExact(base).toString())));
          Optional
              .ofNullable(row.getResource(varDateNotEarlier(base).toString()))
              .ifPresent(resDate -> model
                  .add(evt, Core.takesPlaceNotEarlierThan, resDate)
                  .add(resDate, RDF.type, Core.date)
                  .add(resDate, Core.hasDateTime,
                      row.getLiteral(varDateTimeNotEarlier(base).toString())));
          Optional
              .ofNullable(row.getResource(varDateNotLater(base).toString()))
              .ifPresent(resDate -> model
                  .add(evt, Core.takesPlaceNotLaterThan, resDate)
                  .add(resDate, RDF.type, Core.date)
                  .add(resDate, Core.hasDateTime,
                      row.getLiteral(varDateTimeNotLater(base).toString())));
          Optional
              .ofNullable(row.getResource(varDateSort(base).toString()))
              .ifPresent(resDate -> model
                  .add(evt, Core.hasSortingDate, resDate)
                  .add(resDate, RDF.type, Core.date)
                  .add(resDate, Core.hasDateTime,
                      row.getLiteral(varDateTimeSort(base).toString())));
        });
  }

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #aspect}")
  public String findAll(QueryParameters params, Lang lang, Optional<Resource> aspect) {
    HydraCollectionBuilder builder = hydraBuilderFactory.collectionBuilder(ENDPOINT_NAME,
        Core.person, Api.personOrderByVar, params);
    // Add aspect query
    builder.mapper.add("aspect", Api.personAspectVar, aspect);
    aspect.ifPresent(resAspect -> {
      Path path = PathFactory.pathSeq(PathFactory.pathLink(Core.hasAspectChangedIn.asNode()),
          PathFactory.pathLink(Core.usesAspect.asNode()));
      builder.coreData.addWhere(VAR_MAIN, path, resAspect);
    });
    builder.extendedData.addWhere(dataWhere());
    return builder.query(ROW_MAPPER, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder = hydraBuilderFactory.singleBuilder(ENDPOINT_NAME, id, Core.person);
    builder.coreData.addWhere(dataWhere());
    return builder.query(ROW_MAPPER, lang);
  }

  private WhereBuilder dataWhere() {
    return new WhereBuilder()
        .addWhere(eventWhere(PREF_BIRTH, Core.isBornIn))
        .addWhere(eventWhere(PREF_DEATH, Core.diesIn))
        .addOptional(VAR_MAIN, Core.sameAs, VAR_SAME_AS);
  }

  private WhereBuilder eventWhere(String base, Property type) {
    Node var = NodeFactory.createVariable(base);
    return new WhereBuilder()
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, type, var)
            .addWhere(var, RDFS.label, varEventLabel(base))
            .addOptional(new WhereBuilder()
                .addWhere(var, Core.takesPlaceOn, varDateExact(base))
                .addWhere(varDateExact(base), Core.hasDateTime, varDateTimeExact(base)))
            .addOptional(new WhereBuilder()
                .addWhere(var, Core.takesPlaceNotEarlierThan, varDateNotEarlier(base))
                .addWhere(varDateNotEarlier(base), Core.hasDateTime, varDateTimeNotEarlier(base)))
            .addOptional(new WhereBuilder()
                .addWhere(var, Core.takesPlaceNotLaterThan, varDateNotLater(base))
                .addWhere(varDateNotLater(base), Core.hasDateTime, varDateTimeNotLater(base)))
            .addOptional(new WhereBuilder()
                .addWhere(var, Core.hasSortingDate, varDateSort(base))
                .addWhere(varDateSort(base), Core.hasDateTime, varDateTimeSort(base))));
  }

  private static Node varDateExact(String base) {
    return NodeFactory.createVariable(base + "_exact");
  }

  private static Node varDateNotEarlier(String base) {
    return NodeFactory.createVariable(base + "_notEarlier");
  }

  private static Node varDateNotLater(String base) {
    return NodeFactory.createVariable(base + "_notLater");
  }

  private static Node varDateSort(String base) {
    return NodeFactory.createVariable(base + "_sort");
  }

  private static Node varDateTimeExact(String base) {
    return NodeFactory.createVariable(base + "_exactDateTime");
  }

  private static Node varDateTimeNotEarlier(String base) {
    return NodeFactory.createVariable(base + "_notEarlierDateTime");
  }

  private static Node varDateTimeNotLater(String base) {
    return NodeFactory.createVariable(base + "_notLaterDateTime");
  }

  private static Node varDateTimeSort(String base) {
    return NodeFactory.createVariable(base + "_sortDateTime");
  }

  private static Node varEventLabel(String base) {
    return NodeFactory.createVariable(base + "_label");
  }
}
