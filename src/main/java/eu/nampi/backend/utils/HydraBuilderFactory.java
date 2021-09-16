package eu.nampi.backend.utils;

import java.util.List;
import java.util.UUID;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.queryBuilder.HydraCollectionBuilder;
import eu.nampi.backend.queryBuilder.HydraDeleteBuilder;
import eu.nampi.backend.queryBuilder.HydraInsertBuilder;
import eu.nampi.backend.queryBuilder.HydraSingleBuilder;
import eu.nampi.backend.queryBuilder.HydraUpdateBuilder;
import eu.nampi.backend.repository.HierarchyRepository;
import eu.nampi.backend.service.JenaService;

@Component
public class HydraBuilderFactory {

  @Autowired
  Serializer serializer;

  @Autowired
  JenaService jenaService;

  @Autowired
  UrlBuilder urlBuilder;

  @Autowired
  HierarchyRepository hierarchyRepository;

  /**
   * Hydra Collection Builders
   */

  public HydraCollectionBuilder collectionBuilder(String endpointName, Resource mainType,
      Resource orderByVar, QueryParameters params, boolean includeTextFilter,
      boolean includeTypeAndText) {
    return new HydraCollectionBuilder(jenaService, serializer, urlBuilder.endpointUri(endpointName),
        mainType, orderByVar, params, includeTextFilter, includeTypeAndText);
  }

  public HydraCollectionBuilder collectionBuilder(String endpointName, Resource mainType,
      Resource orderByVar, QueryParameters params, boolean includeTextFilter) {
    return collectionBuilder(endpointName, mainType, orderByVar, params, includeTextFilter, true);
  }

  public HydraCollectionBuilder collectionBuilder(String endpointName, Resource mainType,
      Resource orderByVar, QueryParameters params) {
    return collectionBuilder(endpointName, mainType, orderByVar, params, true, true);
  }

  /**
   * Hydra Single Builders
   */

  public HydraSingleBuilder singleBuilder(String endpointName, UUID id, Resource mainType,
      boolean filterBasic) {
    return new HydraSingleBuilder(jenaService, serializer, urlBuilder.endpointUri(endpointName, id),
        mainType, filterBasic);
  }

  public HydraSingleBuilder singleBuilder(String endpointName, UUID id, Resource mainType) {
    return singleBuilder(endpointName, id, mainType, true);
  }

  public HydraSingleBuilder singleBuilder(String endpointName, Resource mainType,
      boolean filterBasic) {
    return new HydraSingleBuilder(jenaService, serializer, urlBuilder.endpointUri(endpointName),
        mainType, filterBasic);
  }

  /**
   * Hydra Insert Builders
   */

  public HydraInsertBuilder insertBuilder(Lang lang, String endpointName, Resource mainType,
      List<Literal> labels, List<Literal> comments, List<Literal> texts) {
    UUID id = UUID.randomUUID();
    return new HydraInsertBuilder(jenaService, hierarchyRepository, lang,
        urlBuilder.endpointUri(endpointName, id), mainType, labels, comments, texts, id);
  }

  /**
   * Hydra Update Builders
   */

  public HydraUpdateBuilder updateBuilder(Lang lang, UUID id, String endpointName,
      Resource mainType, List<Literal> labels, List<Literal> comments, List<Literal> texts) {
    return new HydraUpdateBuilder(jenaService, hierarchyRepository, lang,
        urlBuilder.endpointUri(endpointName, id), mainType, labels, comments, texts, id);
  }

  /**
   * Hydra Delete Builders
   */

  public HydraDeleteBuilder deleteBuilder(UUID id, String endpointName, Resource mainType) {
    return new HydraDeleteBuilder(jenaService, hierarchyRepository,
        urlBuilder.endpointUri(endpointName, id), mainType, id);
  }
}
