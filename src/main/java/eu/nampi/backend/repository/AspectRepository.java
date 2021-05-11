package eu.nampi.backend.repository;

import java.util.Optional;
import java.util.UUID;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.SchemaOrg;

@Repository
@CacheConfig(cacheNames = "aspects")
public class AspectRepository extends AbstractHydraRepository {

  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");
  private static final Node VAR_STRING = NodeFactory.createVariable("string");

  public Model findAll(QueryParameters params, Optional<String> participant) {
    HydraCollectionBuilder builder = new HydraCollectionBuilder(endpointUri("aspects"), Core.aspect,
        Api.aspectOrderByVar, params, false, false);
    ExprFactory ef = builder.ef;
    Node varMain = HydraCollectionBuilder.VAR_MAIN;

    if (params.getText().isPresent()) {
      Node varSearchString = NodeFactory.createVariable("searchString");
      Path path = PathFactory.pathAlt(PathFactory.pathLink(RDFS.label.asNode()),
          PathFactory.pathLink(Core.hasXsdString.asNode()));
      Expr regex = ef.regex(varSearchString, params.getText().get(), "i");
      builder.dataWhere.addOptional(varMain, path, varSearchString).addFilter(regex);
      builder.countWhere.addOptional(varMain, path, varSearchString).addFilter(regex);
    }
    builder.dataWhere.addOptional(varMain, Core.hasXsdString, VAR_STRING);

    if (participant.isPresent()) {
      Path path = PathFactory.pathSeq(PathFactory.pathLink(Core.aspectIsUsedIn.asNode()),
          PathFactory.pathLink(Core.hasParticipant.asNode()));
      builder.dataWhere.addWhere(varMain, path, ResourceFactory.createResource(participant.get()));
      builder.countWhere.addWhere(varMain, path, ResourceFactory.createResource(participant.get()));
    }

    builder.dataWhere.addOptional(varMain, SchemaOrg.sameAs, VAR_SAME_AS);

    addData(builder, varMain);

    builder.mapper.add("participant", Api.aspectParticipantVar, participant.orElse(""));

    return construct(builder);
  }

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #person}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> person) {
    Model model = findAll(params, person);
    return serialize(model, lang, ResourceFactory.createResource(endpointUri("aspects")));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder =
        new HydraSingleBuilder(individualsUri(Core.aspect, id), Core.aspect);
    addData(builder, HydraSingleBuilder.VAR_MAIN);
    builder.addOptional(HydraSingleBuilder.VAR_MAIN, SchemaOrg.sameAs, VAR_SAME_AS)
        .addOptional(HydraSingleBuilder.VAR_MAIN, Core.hasXsdString, VAR_STRING);
    addData(builder, HydraSingleBuilder.VAR_MAIN);
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(builder.iri));
  }

  private void addData(ConstructBuilder builder, Node varMain) {
    builder.addConstruct(varMain, SchemaOrg.sameAs, VAR_SAME_AS).addConstruct(varMain,
        Core.hasXsdString, VAR_STRING);
  }
}
