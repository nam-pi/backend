package eu.nampi.backend.model.hydra.temp;

import java.util.function.BiFunction;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import eu.nampi.backend.service.JenaService;

public class HydraSingleBuilder extends AbstractHydraBuilder {

  public HydraSingleBuilder(JenaService jenaService, String baseUri, Resource mainType,
      boolean optionalLabel) {
    super(jenaService, baseUri, mainType, optionalLabel);
    dataSelect.addFilter(ef.sameTerm(VAR_MAIN, root));
  }

  public HydraSingleBuilder(JenaService jenaService, String baseUri, Resource mainType) {
    this(jenaService, baseUri, mainType, false);
  }

  @Override
  public void build(BiFunction<Model, QuerySolution, RDFNode> rowToNode) {
    jenaService.select(dataSelect, row -> rowToNode.apply(this.model, row));
  }

}
