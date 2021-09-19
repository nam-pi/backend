package eu.nampi.backend.queryBuilder;

import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import eu.nampi.backend.repository.HierarchyRepository;
import eu.nampi.backend.repository.TypeRepository;
import eu.nampi.backend.service.JenaService;

public abstract class AbstractHydraUpdateBuilder extends AbstractHydraBuilder {

  private HierarchyRepository hierarchyRepository;

  private TypeRepository typeRepository;

  public UpdateBuilder updateBuilder = new UpdateBuilder();

  public static final Node VAR_PREDICATE = NodeFactory.createVariable("predicate");
  public static final Node VAR_OBJECT = NodeFactory.createVariable("object");

  protected AbstractHydraUpdateBuilder(JenaService jenaService,
      HierarchyRepository hierarchyRepository, TypeRepository typeRepository, String baseUri,
      Resource mainType) {
    super(jenaService, baseUri, mainType);
    this.hierarchyRepository = hierarchyRepository;
    this.typeRepository = typeRepository;
    this.ef = updateBuilder.getExprFactory();
  }

  public void build() {
    jenaService.update(updateBuilder);
  }

  public void validateSubnode(RDFNode parent, RDFNode child) {
    if (!hierarchyRepository.isSubnode(parent, child)) {
      throw new IllegalArgumentException(
          String.format("'%s' is not a subtype of '%s'.", child.toString(), parent.toString()));
    }
  }

  public void validateType(RDFNode type, RDFNode node) {
    if (!typeRepository.isType(type, node)) {
      throw new IllegalArgumentException(
          String.format("'%s' is not an individual of '%s'", node.toString(), type.toString()));
    }
  }

  public UpdateBuilder addInsert(Object s, Object p, Object o) {
    return updateBuilder.addInsert(s, p, o);
  }
}
