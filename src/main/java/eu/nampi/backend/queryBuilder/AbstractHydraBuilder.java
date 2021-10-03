package eu.nampi.backend.queryBuilder;

import java.util.Optional;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import eu.nampi.backend.service.JenaService;

public abstract class AbstractHydraBuilder {

  protected JenaService jenaService;

  protected String baseUri;
  public ExprFactory ef;
  public Resource root;

  public static final Node VAR_MAIN = NodeFactory.createVariable("main");
  public static final Node VAR_COMMENT = NodeFactory.createVariable("comment");
  public static final Node VAR_LABEL = NodeFactory.createVariable("label");
  public static final Node VAR_TEXT = NodeFactory.createVariable("text");
  public static final Node VAR_TYPE = NodeFactory.createVariable("type");


  protected AbstractHydraBuilder(JenaService jenaService, String baseUri, Resource mainType) {
    this.root = ResourceFactory.createResource(baseUri);
    this.jenaService = jenaService;
  }

  protected Optional<RDFNode> get(QuerySolution row, Node variable) {
    return Optional.ofNullable(row.get(variable.getName()));
  }

  public boolean ask(AskBuilder builder) {
    return jenaService.ask(builder);
  }
}
