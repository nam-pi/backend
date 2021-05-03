package eu.nampi.backend.model.hydra;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;

public class HydraSingleBuilder extends AbstractHydraBuilder {

  public HydraSingleBuilder(String iri, Property type) {
    super(NodeFactory.createURI(iri), type);
    addWhere(mainWhere());
    addFilter(ef.sameTerm(VAR_MAIN, baseNode));
    addWhere(labelWhere());
  }

  public String buildHydra() {
    return buildString();
  }

}
