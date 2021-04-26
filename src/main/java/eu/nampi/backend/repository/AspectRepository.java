package eu.nampi.backend.repository;

import static eu.nampi.backend.model.hydra.AbstractHydraBuilder.MAIN_SUBJ;
import java.util.Optional;
import java.util.UUID;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
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
@CacheConfig(cacheNames = "aspects")
public class AspectRepository extends AbstractHydraRepository {

  public Model findAll(QueryParameters params, Optional<String> person) {
    HydraCollectionBuilder hydra;
    if (params.getText().isPresent()) {
      hydra = new HydraCollectionBuilder(params, Core.aspect, Doc.aspectOrderByVar,
          Optional.of("regex(?t, '%s', 'i')")).addMainOptional("rdfs:label|core:has_xsd_string",
              "?t");
    } else {
      hydra = new HydraCollectionBuilder(params, Core.aspect, Doc.aspectOrderByVar);
    }
    person.ifPresentOrElse(
        p -> hydra.addWhere("?e", Core.usesAspect, MAIN_SUBJ)
            .addWhere("?e", Core.hasMainParticipant, "<" + p + ">")
            .addSearchVariable("person", Doc.aspectPersonVar, false, "'" + p + "'"),
        () -> hydra.addSearchVariable("person", Doc.aspectPersonVar, false));
    addData(hydra);
    return construct(hydra);
  }

  @Cacheable(
      key = "{#lang, #params.limit, #params.offset, #params.orderByClauses, #params.type, #params.text, #person}")
  public String findAll(QueryParameters params, Lang lang, Optional<String> person) {
    Model model = findAll(params, person);
    return serialize(model, lang, ResourceFactory.createResource(params.getBaseUrl()));
  }

  @Cacheable(key = "{#lang, #id}")
  public String findOne(Lang lang, UUID id) {
    String uri = individualsUri(Core.aspect, id);
    HydraSingleBuilder builder = new HydraSingleBuilder(uri, Core.aspect);
    addData(builder);
    Model model = construct(builder);
    return serialize(model, lang, ResourceFactory.createResource(uri));
  }

  private void addData(AbstractHydraBuilder<?> builder) {
    builder.addMainOptional(Core.hasXsdString, "?string")
        .addMainConstruct(Core.hasXsdString, "?string").addMainConstruct(SchemaOrg.sameAs, "?sa")
        .addMainOptional(SchemaOrg.sameAs, "?sa");
  }
}
