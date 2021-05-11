package eu.nampi.backend.model.hydra.temp;

import java.util.Optional;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.SchemaOrg;

public abstract class AbstractHydraBuilder implements InterfaceHydraBuilder {

  public static final Node VAR_MAIN = NodeFactory.createVariable("main");
  public static final Node VAR_LABEL = NodeFactory.createVariable("label");
  public static final Node VAR_COMMENT = NodeFactory.createVariable("comment");

  protected JenaService jenaService;

  public Model model = ModelFactory.createDefaultModel();

  public SelectBuilder dataSelect = new SelectBuilder();

  public ExprFactory ef;

  protected Resource mainType;

  protected String baseUri;

  public Resource root;

  protected AbstractHydraBuilder(JenaService jenaService, String baseUri, Resource mainType) {
    this.jenaService = jenaService;
    this.baseUri = baseUri;
    this.mainType = mainType;
    this.root = ResourceFactory.createResource(baseUri);
    dataSelect
        .addPrefix("api", Api.getURI())
        .addPrefix("core", Core.getURI())
        .addPrefix("hydra", Hydra.getURI())
        .addPrefix("rdf", RDF.getURI())
        .addPrefix("rdfs", RDFS.getURI())
        .addPrefix("schema", SchemaOrg.getURI())
        .addPrefix("xsd", XSD.getURI())
        .addWhere(VAR_MAIN, RDF.type, mainType)
        .addWhere(VAR_MAIN, RDFS.label, VAR_LABEL);
    model
        .setNsPrefix("api", Api.getURI())
        .setNsPrefix("core", Core.getURI())
        .setNsPrefix("hydra", Hydra.getURI())
        .setNsPrefix("rdf", RDF.getURI())
        .setNsPrefix("rdfs", RDFS.getURI())
        .setNsPrefix("schema", SchemaOrg.getURI())
        .setNsPrefix("xsd", XSD.getURI());
    this.ef = dataSelect.getExprFactory();
  }

  protected Optional<RDFNode> get(QuerySolution row, Node variable) {
    return Optional.ofNullable(row.get(variable.getName()));
  }

}
