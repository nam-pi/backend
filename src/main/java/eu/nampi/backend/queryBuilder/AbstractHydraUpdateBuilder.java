package eu.nampi.backend.queryBuilder;

import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import eu.nampi.backend.repository.HierarchyRepository;
import eu.nampi.backend.service.JenaService;

public abstract class AbstractHydraUpdateBuilder extends AbstractHydraBuilder {

  private HierarchyRepository hierarchyRepository;

  public UpdateBuilder updateBuilder = new UpdateBuilder();

  public static final Node VAR_PREDICATE = NodeFactory.createVariable("predicate");
  public static final Node VAR_OBJECT = NodeFactory.createVariable("object");

  protected AbstractHydraUpdateBuilder(JenaService jenaService,
      HierarchyRepository hierarchyRepository, String baseUri, Resource mainType) {
    super(jenaService, baseUri, mainType);
    this.hierarchyRepository = hierarchyRepository;
    this.ef = updateBuilder.getExprFactory();
  }

  public void build() {
    jenaService.update(updateBuilder);
  }

  public void validateSubtype(RDFNode parent, RDFNode child) {
    if (!hierarchyRepository.isSubtype(parent, child)) {
      throw new IllegalArgumentException(
          String.format("'%s' is not a subtype of '%s'.", child.toString(), parent.toString()));
    }
  }
}