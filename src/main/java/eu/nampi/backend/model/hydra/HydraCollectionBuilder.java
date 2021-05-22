package eu.nampi.backend.model.hydra;

import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Hydra;

public class HydraCollectionBuilder extends AbstractHydraBuilder {
  private Resource orderByVar;
  protected QueryParameters params;
  public ParameterMapper mapper;
  public WhereBuilder extendedData = new WhereBuilder();

  public HydraCollectionBuilder(JenaService jenaService, String baseUri, Resource mainType,
      Resource orderByVar, QueryParameters params, boolean includeTextFilter) {
    super(jenaService, baseUri, mainType);
    this.mapper = new ParameterMapper(baseUri, root, model);
    this.orderByVar = orderByVar;
    this.params = params;

    // Set up manages node
    Resource manages = ResourceFactory.createResource();
    this.model
        .add(root, Hydra.manages, manages)
        .add(manages, Hydra.object, mainType);

    boolean orderByLabel = this.params.getOrderByClauses().containsKey("label");
    if (orderByLabel) {
      coreData.addWhere(VAR_MAIN, RDFS.label, VAR_LABEL);
    }

    // Add default data
    extendedData
        .addOptional(VAR_MAIN, RDFS.label, VAR_LABEL)
        .addOptional(VAR_MAIN, RDFS.comment, VAR_COMMENT);

    // Add default text filter
    params.getText().filter(text -> includeTextFilter).ifPresent(text -> {
      if (!orderByLabel) {
        coreData.addWhere(VAR_MAIN, RDFS.label, VAR_LABEL);
      }
      Expr matchText = ef.regex(VAR_LABEL, text, "i");
      coreData.addFilter(matchText);
    });

    // Add type filter
    params.getType().map(ResourceFactory::createResource).ifPresent(res -> {
      coreData.addWhere(VAR_MAIN, RDF.type, res);
    });
  }

  public HydraCollectionBuilder(JenaService jenaService, String baseUri, Resource mainType,
      Resource orderByVar, QueryParameters params) {
    this(jenaService, baseUri, mainType, orderByVar, params, true);
  }

  @Override
  public void build(BiFunction<Model, QuerySolution, RDFNode> rowToNode) {
    // Count all possible matches
    int totalItems = jenaService.count(coreData, VAR_MAIN);

    // Finalize the core select
    SelectBuilder coreSelect = new SelectBuilder()
        .setDistinct(true)
        .addVar(VAR_MAIN)
        .addWhere(coreData);

    this.params
        .getOrderByClauses()
        .appendAllTo(coreSelect);
    coreSelect
        .addOrderBy(VAR_MAIN)
        .setOffset(params.getOffset())
        .setLimit(params.getLimit());

    SelectBuilder finalSelect = new SelectBuilder()
        .addSubQuery(coreSelect)
        .addWhere(extendedData);

    // Setup the root hydra collection
    this.model
        .add(this.root, RDF.type, Hydra.Collection)
        .addLiteral(root, Hydra.totalItems,
            ResourceFactory.createTypedLiteral(String.valueOf(totalItems), XSDDatatype.XSDinteger));

    // Query the data using the jena service and add the content provided by the row mapper function
    // to the model
    jenaService.select(finalSelect, row -> this.model
        .add(root, Hydra.member, rowToNode.apply(this.model, row)));

    // Set up the search and view nodes with the main query parameters
    this.mapper
        .add("limit", Hydra.limit, params.getLimit())
        .add("offset", Hydra.offset, params.getOffset())
        .add("orderBy", orderByVar, params.getOrderByClauses().toQueryString())
        .add("pageIndex", Hydra.pageIndex, Optional.empty())
        .add("text", Api.textVar, params.getText())
        .add("type", RDF.type, params.getType())
        .insertTemplate()
        .insertView(totalItems);
  }
}
