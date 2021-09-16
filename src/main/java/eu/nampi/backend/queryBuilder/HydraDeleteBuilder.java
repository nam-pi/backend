package eu.nampi.backend.queryBuilder;

import java.util.UUID;
import org.apache.jena.rdf.model.Resource;
import eu.nampi.backend.repository.HierarchyRepository;
import eu.nampi.backend.service.JenaService;

public class HydraDeleteBuilder extends AbstractHydraUpdateBuilder {

  public UUID id;

  public HydraDeleteBuilder(JenaService jenaService, HierarchyRepository hierarchyRepository,
      String baseUri, Resource mainType, UUID id) {
    super(jenaService, hierarchyRepository, baseUri, mainType);
    this.id = id;
    updateBuilder
        .addFilter(ef.sameTerm(VAR_MAIN, root))
        .addWhere(VAR_MAIN, VAR_PREDICATE, VAR_OBJECT)
        .addDelete(VAR_MAIN, VAR_PREDICATE, VAR_OBJECT);
  }
}
