package eu.nampi.backend.model.hydra;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.SchemaOrg;

public abstract class AbstractHydraBuilder extends ConstructBuilder implements InterfaceHydraBuilder {

  public static final Node VAR_MAIN = NodeFactory.createVariable("main");
  public static final Node VAR_MAIN_LABEL = NodeFactory.createVariable("label");
  public static final Node VAR_MAIN_COMMENT = NodeFactory.createVariable("comment");

  public final ExprFactory ef;
  public final Node baseNode;
  public final Property mainType;

  public AbstractHydraBuilder(Node baseNode, Property mainType) {
    super();
    // @formatter:off
    this
        .addPrefix("core", Core.getURI())
        .addPrefix("doc", Doc.getURI())
        .addPrefix("hydra", Hydra.getURI())
        .addPrefix("rdf", RDF.getURI())
        .addPrefix("rdfs", RDFS.getURI())
        .addPrefix("schema", SchemaOrg.getURI())
        .addPrefix("xsd", XSD.getURI())
        .addConstruct(VAR_MAIN, RDF.type, mainType)
        .addConstruct(VAR_MAIN, RDFS.label, VAR_MAIN_LABEL)
        .addConstruct(VAR_MAIN, RDFS.comment, VAR_MAIN_COMMENT);
    // @formatter:on
    this.baseNode = baseNode;
    this.ef = this.getExprFactory();
    this.mainType = mainType;
  }

  public WhereBuilder mainWhere() {
    return new WhereBuilder().addWhere(VAR_MAIN, RDF.type, mainType);
  }

  public WhereBuilder labelWhere() {
    return new WhereBuilder().addWhere(VAR_MAIN, RDFS.label, VAR_MAIN_LABEL);
  }

  public WhereBuilder commentWhere() {
    return new WhereBuilder().addOptional(VAR_MAIN, RDFS.comment, VAR_MAIN_COMMENT);
  }

}
