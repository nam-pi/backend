package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder.VAR_MAIN;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.temp.AbstractHydraBuilder;
import eu.nampi.backend.model.hydra.temp.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.temp.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.SchemaOrg;

@Repository
@CacheConfig(cacheNames = "groups")
public class GroupRepository extends AbstractHydraRepository {

  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");

  private static final BiFunction<Model, QuerySolution, RDFNode> ROW_MAPPER = (model, row) -> {
    Resource main = row.getResource(VAR_MAIN.toString());
    // Main
    model.add(main, RDF.type, Core.group);
    // Label
    Optional.ofNullable(row.getLiteral(VAR_LABEL.toString())).map(Literal::getString)
        .ifPresent(label -> model.add(main, RDFS.label, label));
    // Comment
    Optional.ofNullable(row.getLiteral(VAR_COMMENT.toString())).map(Literal::getString)
        .ifPresent(comment -> model.add(main, RDFS.comment, comment));
    // SameAs
    Optional.ofNullable(row.getResource(VAR_SAME_AS.toString())).map(Resource::getURI)
        .ifPresent(string -> model.add(main, SchemaOrg.sameAs, string));
    return main;
  };

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text}")
  public String findAll(QueryParameters params, Lang lang) {
    HydraCollectionBuilder builder = new HydraCollectionBuilder(jenaService, endpointUri("groups"),
        Core.group, Api.groupOrderByVar, params);
    builder.extendedData.addOptional(VAR_MAIN, SchemaOrg.sameAs, VAR_SAME_AS);
    return build(builder, lang);
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder =
        new HydraSingleBuilder(jenaService, individualsUri(Core.group, id), Core.group);
    builder.coreData.addOptional(VAR_MAIN, SchemaOrg.sameAs, VAR_SAME_AS);
    return build(builder, lang);
  }

  private String build(AbstractHydraBuilder builder, Lang lang) {
    builder.build(ROW_MAPPER);
    return serialize(builder.model, lang, builder.root);
  }

}
