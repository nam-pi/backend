package eu.nampi.backend.repository;

import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_MAIN;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_TYPE;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.exception.DeletionNotPermittedException;
import eu.nampi.backend.model.InsertResult;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.queryBuilder.HydraBuilderFactory;
import eu.nampi.backend.queryBuilder.HydraCollectionBuilder;
import eu.nampi.backend.queryBuilder.HydraDeleteBuilder;
import eu.nampi.backend.queryBuilder.HydraInsertBuilder;
import eu.nampi.backend.queryBuilder.HydraSingleBuilder;
import eu.nampi.backend.queryBuilder.HydraUpdateBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;

@Repository
@CacheConfig(cacheNames = "groups")
public class GroupRepository {

  @Autowired
  HydraBuilderFactory hydraBuilderFactory;

  private static final String ENDPOINT_NAME = "groups";
  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");
  private static final Node VAR_PART_OF = NodeFactory.createVariable("partOf");
  private static final Node VAR_PART_OF_LABEL = NodeFactory.createVariable("partOfLabel");
  private static final Node VAR_PART_OF_TYPE = NodeFactory.createVariable("partOfType");
  private static final Node VAR_HAS_PART = NodeFactory.createVariable("hasPart");
  private static final Node VAR_HAS_PART_LABEL = NodeFactory.createVariable("hasPartLabel");
  private static final Node VAR_HAS_PART_TYPE = NodeFactory.createVariable("hasPartType");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    Optional
        .ofNullable(row.getResource(VAR_TYPE.toString()))
        .ifPresentOrElse(type -> model.add(main, RDF.type, type),
            () -> model.add(main, RDF.type, Core.group));
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
    // Part of
    Optional
        .ofNullable(row.getResource(VAR_PART_OF.toString()))
        .ifPresent(iri -> {
          model.add(main, Core.isPartOf, iri);
          Optional.ofNullable(row.getResource(VAR_PART_OF_TYPE.toString()))
              .ifPresentOrElse(type -> model.add(iri, RDF.type, type),
                  () -> model.add(iri, RDF.type, Core.group));
          Optional.ofNullable(row.getLiteral(VAR_PART_OF_LABEL.toString()))
              .ifPresent(label -> model.add(iri, RDFS.label, label));
        });
    // Has part
    Optional
        .ofNullable(row.getResource(VAR_HAS_PART.toString()))
        .ifPresent(iri -> {
          model.add(main, Core.hasPart, iri);
          Optional.ofNullable(row.getResource(VAR_HAS_PART_TYPE.toString()))
              .ifPresentOrElse(type -> model.add(iri, RDF.type, type),
                  () -> model.add(iri, RDF.type, Core.group));
          Optional.ofNullable(row.getLiteral(VAR_HAS_PART_LABEL.toString()))
              .ifPresent(label -> model.add(iri, RDFS.label, label));
        });
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #partOf, #hasPart}")
  public String findAll(QueryParameters params, Lang lang, Optional<Resource> partOf,
      Optional<Resource> hasPart) {
    HydraCollectionBuilder builder = hydraBuilderFactory.collectionBuilder(ENDPOINT_NAME,
        Core.group, Api.groupOrderByVar, params);
    ExprFactory ef = builder.ef;
    builder.extendedData.addOptional(VAR_MAIN, Core.sameAs, VAR_SAME_AS);
    // Part of
    builder.mapper.add("partOf", Api.groupPartOfVar, partOf);
    partOf.ifPresent(partOfType -> builder.coreData
        .addWhere(VAR_MAIN, Core.isPartOf, VAR_PART_OF)
        .addFilter(ef.sameTerm(VAR_PART_OF, partOfType))
        .addWhere(VAR_PART_OF, RDFS.label, VAR_PART_OF_LABEL));
    // Has part
    builder.mapper.add("hasPart", Api.groupHasPartVar, hasPart);
    hasPart.ifPresent(hasPartType -> builder.coreData
        .addWhere(VAR_MAIN, Core.hasPart, VAR_HAS_PART)
        .addFilter(ef.sameTerm(VAR_HAS_PART, hasPartType))
        .addWhere(VAR_HAS_PART, RDFS.label, VAR_HAS_PART_LABEL));
    return builder.query(ROW_MAPPER, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder = hydraBuilderFactory.singleBuilder(ENDPOINT_NAME, id, Core.group);
    ExprFactory ef = builder.ef;
    builder.coreData.addOptional(VAR_MAIN, Core.sameAs, VAR_SAME_AS)
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.isPartOf, VAR_PART_OF)
            .addWhere(VAR_PART_OF, RDF.type, VAR_PART_OF_TYPE)
            .addFilter(ef.not(ef.strstarts(ef.str(VAR_PART_OF_TYPE), OWL.getURI())))
            .addFilter(ef.not(ef.strstarts(ef.str(VAR_PART_OF_TYPE), RDFS.getURI())))
            .addFilter(ef.not(ef.strstarts(ef.str(VAR_PART_OF_TYPE), RDF.getURI())))
            .addWhere(VAR_PART_OF, RDFS.label, VAR_PART_OF_LABEL))
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, Core.hasPart, VAR_HAS_PART)
            .addWhere(VAR_HAS_PART, RDF.type, VAR_HAS_PART_TYPE)
            .addFilter(ef.not(ef.strstarts(ef.str(VAR_HAS_PART_TYPE), OWL.getURI())))
            .addFilter(ef.not(ef.strstarts(ef.str(VAR_HAS_PART_TYPE), RDFS.getURI())))
            .addFilter(ef.not(ef.strstarts(ef.str(VAR_HAS_PART_TYPE), RDF.getURI())))
            .addWhere(VAR_HAS_PART, RDFS.label, VAR_HAS_PART_LABEL));
    return builder.query(ROW_MAPPER, lang);
  }

  public InsertResult insert(Lang lang, Resource type, List<Literal> labels, List<Literal> comments,
      List<Literal> texts, List<Resource> sameAs, List<Resource> partOf) {
    HydraInsertBuilder builder = hydraBuilderFactory.insertBuilder(lang, ENDPOINT_NAME, type,
        labels, comments, texts, sameAs);
    builder.validateSubnode(Core.group, type);
    partOf.forEach(parent -> {
      builder.validateType(Core.group, parent);
      builder.addInsert(builder.root, Core.isPartOf, parent);
    });
    builder.build();
    return new InsertResult(builder.root, findOne(lang, builder.id));
  }

  public String update(Lang lang, UUID id, Resource type, List<Literal> labels,
      List<Literal> comments, List<Literal> texts, List<Resource> sameAs, List<Resource> partOf) {
    HydraUpdateBuilder builder = hydraBuilderFactory.updateBuilder(lang, id, ENDPOINT_NAME, type,
        labels, comments, texts, sameAs);
    builder.validateSubnode(Core.group, type);
    partOf.forEach(parent -> {
      builder.validateType(Core.group, parent);
      builder.addInsert(builder.root, Core.isPartOf, parent);
    });
    builder.build();
    return findOne(lang, builder.id);
  }

  public void delete(UUID id) {
    HydraDeleteBuilder builder = hydraBuilderFactory.deleteBuilder(id, ENDPOINT_NAME, Core.group);
    ExprFactory ef = builder.updateBuilder.getExprFactory();
    Node varProperty = NodeFactory.createVariable("property");
    if (builder.ask(new AskBuilder()
        .addWhere("?item", varProperty, builder.root)
        .addFilter(ef.in(varProperty, Core.hasParticipant, Core.isPartOf)))) {
      throw new DeletionNotPermittedException("The group to be deleted is still in use");
    }
    builder.build();
  }
}
