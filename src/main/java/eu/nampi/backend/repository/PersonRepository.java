package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.MAIN_SUBJ;
import java.util.UUID;
import org.apache.jena.arq.querybuilder.WhereBuilder;
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
import eu.nampi.backend.model.hydra.AbstractHydraBuilder;
import eu.nampi.backend.model.hydra.HydraCollectionBuilder;
import eu.nampi.backend.model.hydra.HydraSingleBuilder;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;
import eu.nampi.backend.vocabulary.SchemaOrg;

@Repository
@CacheConfig(cacheNames = "persons")
public class PersonRepository extends AbstractHydraRepository {

  public Model findAll(QueryParameters params) {
    HydraCollectionBuilder hydra =
        new HydraCollectionBuilder(params, Core.person, Doc.personOrderByVar);
    addData(hydra);
    return construct(hydra);
  }

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text}")
  public String findAll(QueryParameters params, Lang lang) {
    Model model = findAll(params);
    return serialize(model, lang, ResourceFactory.createResource(params.getBaseUrl()));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    String uri = individualsUri(Core.person, id);
    HydraSingleBuilder builder = new HydraSingleBuilder(uri, Core.person);
    addData(builder);
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(uri));
  }

  private void addDateData(AbstractHydraBuilder<?> builder, String eventVar, Property type) {
    String e = "?" + eventVar;
    // @formatter:off
    builder
      .addOptional(new WhereBuilder()
        .addWhere(MAIN_SUBJ, type, e)
        .addWhere(e, RDFS.label, e + "_label")
        .addOptional(new WhereBuilder()
          .addWhere(e, Core.takesPlaceOn, e + "_exact")
          .addWhere(e + "_exact", Core.hasXsdDateTime, e + "_exactDateTime"))
        .addOptional(new WhereBuilder()
          .addWhere(e, Core.takesPlaceNotEarlierThan, e + "_notEarlier")
          .addWhere(e + "_notEarlier", Core.hasXsdDateTime, e + "_notEarlierDateTime"))
        .addOptional(new WhereBuilder()
          .addWhere(e, Core.takesPlaceNotLaterThan, e + "_notLater")
          .addWhere(e + "_notLater", Core.hasXsdDateTime, e + "_notLaterDateTime"))
        .addOptional(new WhereBuilder()
          .addWhere(e, Core.hasSortingDate, e + "_sort")
          .addWhere(e + "_sort", Core.hasXsdDateTime, e + "_sortDateTime")))
      .addConstruct(MAIN_SUBJ, type, e)
      .addConstruct(e, RDF.type, Core.event)
      .addConstruct(e, RDFS.label, e + "_label")
      .addConstruct(e, Core.takesPlaceOn, e + "_exact")
      .addConstruct(e + "_exact", RDF.type, Core.date)
      .addConstruct(e + "_exact", Core.hasXsdDateTime, e + "_exactDateTime")
      .addConstruct(e, Core.takesPlaceNotEarlierThan, e + "_notEarlier")
      .addConstruct(e + "_notEarlier", RDF.type, Core.date)
      .addConstruct(e + "_notEarlier", Core.hasXsdDateTime, e + "_notEarlierDateTime")
      .addConstruct(e, Core.takesPlaceNotLaterThan, e + "_notLater")
      .addConstruct(e + "_notLater", RDF.type, Core.date)
      .addConstruct(e + "_notLater", Core.hasXsdDateTime, e + "_notLaterDateTime")
      .addConstruct(e, Core.hasSortingDate, e + "_sort")
      .addConstruct(e + "_sort", RDF.type, Core.date)
      .addConstruct(e + "_sort", Core.hasXsdDateTime, e + "_sortDateTime");
    // @formatter:on
  }

  private void addData(AbstractHydraBuilder<?> builder) {
    addDateData(builder, "be", Core.isBornIn);
    addDateData(builder, "de", Core.diesIn);
    // @formatter:off
    builder
    // Related to same as
      .addMainConstruct(SchemaOrg.sameAs, "?sa")
      .addMainOptional(SchemaOrg.sameAs, "?sa");
    // @formatter:on
  }
}
