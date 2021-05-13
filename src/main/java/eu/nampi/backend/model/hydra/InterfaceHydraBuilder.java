package eu.nampi.backend.model.hydra;

import java.util.function.BiFunction;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

public interface InterfaceHydraBuilder {

  public void build(BiFunction<Model, QuerySolution, RDFNode> rowToNode);

}
