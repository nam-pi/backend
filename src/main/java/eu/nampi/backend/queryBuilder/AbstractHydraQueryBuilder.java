package eu.nampi.backend.queryBuilder;

import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.util.Serializer;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.SchemaOrg;

public abstract class AbstractHydraQueryBuilder extends AbstractHydraBuilder {

  Serializer serializer;

  protected Resource mainType;
  public Model model = ModelFactory.createDefaultModel();
  public WhereBuilder coreData = new WhereBuilder();

  protected AbstractHydraQueryBuilder(JenaService jenaService, Serializer serializer,
      String baseUri, Resource mainType) {
    super(jenaService, baseUri);
    this.mainType = mainType;
    this.ef = coreData.getExprFactory();
    this.serializer = serializer;
    coreData.addWhere(VAR_MAIN, RDF.type, mainType);
    model
        .setNsPrefix("api", Api.getURI())
        .setNsPrefix("core", Core.getURI())
        .setNsPrefix("hydra", Hydra.getURI())
        .setNsPrefix("owl", OWL.getURI())
        .setNsPrefix("rdf", RDF.getURI())
        .setNsPrefix("rdfs", RDFS.getURI())
        .setNsPrefix("schema", SchemaOrg.getURI())
        .setNsPrefix("xsd", XSD.getURI());
  }

  protected Optional<RDFNode> get(QuerySolution row, Node variable) {
    return Optional.ofNullable(row.get(variable.getName()));
  }

  abstract void build(BiFunction<Model, QuerySolution, RDFNode> rowToNode);

  public String query(BiFunction<Model, QuerySolution, RDFNode> rowToNode, Lang lang) {
    build(rowToNode);
    return serializer.serialize(model, lang, root);
  }

  public String query(BiFunction<Model, QuerySolution, RDFNode> rowToNode, Lang lang,
      Resource customRoot) {
    build(rowToNode);
    return serializer.serialize(model, lang, customRoot);
  }
}
