package eu.nampi.backend.model.hydra;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;

public class HydraSingleBuilder extends AbstractHydraBuilder {

  public final String iri;

  public HydraSingleBuilder(String iri, Resource type) {
    super(NodeFactory.createURI(iri), type);
    this.iri = iri;
    addWhere(mainWhere());
    addFilter(ef.sameTerm(VAR_MAIN, baseNode));
    addWhere(labelWhere());
  }

  public String buildHydra() {
    return buildString();
  }

}
