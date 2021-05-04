package eu.nampi.backend.repository;

import java.util.UUID;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
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
@CacheConfig(cacheNames = "persons")
public class PersonRepository extends AbstractHydraRepository {

  private static final Node VAR_SAME_AS = NodeFactory.createVariable("sameAs");

  public Model findAll(QueryParameters params) {
    HydraCollectionBuilder builder = new HydraCollectionBuilder(endpointUri("persons"), Core.person,
        Api.personOrderByVar, params);
    builder.dataWhere.addWhere(addData(HydraCollectionBuilder.VAR_MAIN));
    addConstruct(builder, HydraCollectionBuilder.VAR_MAIN);
    return construct(builder);
  }

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text}")
  public String findAll(QueryParameters params, Lang lang) {
    Model model = findAll(params);
    return serialize(model, lang, ResourceFactory.createResource(params.getBaseUrl()));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    HydraSingleBuilder builder =
        new HydraSingleBuilder(individualsUri(Core.person, id), Core.person);
    builder.addWhere(addData(HydraSingleBuilder.VAR_MAIN));
    addConstruct(builder, HydraSingleBuilder.VAR_MAIN);
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(builder.iri));
  }

  private void addDateConstruct(ConstructBuilder builder, String varPrefix, Property type,
      Node varMain) {
    // @formatter:off
    Node var = NodeFactory.createVariable(varPrefix);
    Node varLabel = getVarEventLabel(varPrefix);
    Node varDateExact = getVarEventDateExact(varPrefix);
    Node varDateTimeExact = getVarEventDateTimeExact(varPrefix);
    Node varDateNotEarlier = getVarDateNotEarlier(varPrefix);
    Node varDateTimeNotEarlier = getVarDateTimeNotEarlier(varPrefix);
    Node varDateNotLater = getVarDateNotLater(varPrefix);
    Node varDateTimeNotLater = getVarDateTimeNotLater(varPrefix);
    Node varDateSort = getVarDateSort(varPrefix);
    Node varDateTimeSort = getVarDateTimeSort(varPrefix);
    builder
      .addConstruct(varMain, type, var)
      .addConstruct(var, RDF.type, Core.event)
      .addConstruct(var, RDFS.label, varLabel)
      .addConstruct(var, Core.takesPlaceOn, varDateExact)
      .addConstruct(varDateExact, RDF.type, Core.date)
      .addConstruct(varDateExact, Core.hasXsdDateTime, varDateTimeExact)
      .addConstruct(var, Core.takesPlaceNotEarlierThan, varDateNotEarlier)
      .addConstruct(varDateNotEarlier, RDF.type, Core.date)
      .addConstruct(varDateNotEarlier, Core.hasXsdDateTime, varDateTimeNotEarlier)
      .addConstruct(var, Core.takesPlaceNotLaterThan, varDateNotLater)
      .addConstruct(varDateNotLater, RDF.type, Core.date)
      .addConstruct(varDateNotLater, Core.hasXsdDateTime, varDateTimeNotLater)
      .addConstruct(var, Core.hasSortingDate, varDateSort)
      .addConstruct(varDateSort, RDF.type, Core.date)
      .addConstruct(varDateSort, Core.hasXsdDateTime, varDateTimeSort);
    // @formatter:on
  }

  private void addDateData(WhereBuilder builder, String varPrefix, Property type, Node varMain) {
    // @formatter:off
    Node var = NodeFactory.createVariable(varPrefix);
    Node varLabel = getVarEventLabel(varPrefix);
    Node varDateExact = getVarEventDateExact(varPrefix);
    Node varDateTimeExact = getVarEventDateTimeExact(varPrefix);
    Node varDateNotEarlier = getVarDateNotEarlier(varPrefix);
    Node varDateTimeNotEarlier = getVarDateTimeNotEarlier(varPrefix);
    Node varDateNotLater = getVarDateNotLater(varPrefix);
    Node varDateTimeNotLater = getVarDateTimeNotLater(varPrefix);
    Node varDateSort = getVarDateSort(varPrefix);
    Node varDateTimeSort = getVarDateTimeSort(varPrefix);
    builder
      .addOptional(new WhereBuilder()
        .addWhere(varMain, type, var)
        .addWhere(var, RDFS.label, varLabel)
        .addOptional(new WhereBuilder()
          .addWhere(var, Core.takesPlaceOn, varDateExact)
          .addWhere(varDateExact, Core.hasXsdDateTime, varDateTimeExact))
        .addOptional(new WhereBuilder()
          .addWhere(var, Core.takesPlaceNotEarlierThan, varDateNotEarlier)
          .addWhere(varDateNotEarlier, Core.hasXsdDateTime, varDateTimeNotEarlier))
        .addOptional(new WhereBuilder()
          .addWhere(var, Core.takesPlaceNotLaterThan, varDateNotLater)
          .addWhere(varDateNotLater, Core.hasXsdDateTime, varDateTimeNotLater))
        .addOptional(new WhereBuilder()
          .addWhere(var, Core.hasSortingDate, varDateSort)
          .addWhere(varDateSort, Core.hasXsdDateTime, varDateTimeSort)));
    // @formatter:on
  }

  private void addConstruct(ConstructBuilder builder, Node varMain) {
    addDateConstruct(builder, "birth", Core.isBornIn, varMain);
    addDateConstruct(builder, "death", Core.diesIn, varMain);
    builder.addConstruct(varMain, SchemaOrg.sameAs, VAR_SAME_AS);
  }

  private WhereBuilder addData(Node varMain) {
    WhereBuilder builder = new WhereBuilder();
    addDateData(builder, "birth", Core.isBornIn, varMain);
    addDateData(builder, "death", Core.diesIn, varMain);
    builder.addOptional(varMain, SchemaOrg.sameAs, VAR_SAME_AS);
    return builder;
  }

  private Node getVarDateTimeSort(String prefix) {
    return NodeFactory.createVariable(prefix + "_sortDateTime");
  }

  private Node getVarDateSort(String prefix) {
    return NodeFactory.createVariable(prefix + "_sort");
  }

  private Node getVarDateTimeNotLater(String prefix) {
    return NodeFactory.createVariable(prefix + "_notLaterDateTime");
  }

  private Node getVarDateNotLater(String prefix) {
    return NodeFactory.createVariable(prefix + "_notLater");
  }

  private Node getVarDateTimeNotEarlier(String prefix) {
    return NodeFactory.createVariable(prefix + "_notEarlierDateTime");
  }

  private Node getVarDateNotEarlier(String prefix) {
    return NodeFactory.createVariable(prefix + "_notEarlier");
  }

  private Node getVarEventDateTimeExact(String prefix) {
    return NodeFactory.createVariable(prefix + "_exactDateTime");
  }

  private Node getVarEventDateExact(String prefix) {
    return NodeFactory.createVariable(prefix + "_exact");
  }

  private Node getVarEventLabel(String prefix) {
    return NodeFactory.createVariable(prefix + "_label");
  }

}
