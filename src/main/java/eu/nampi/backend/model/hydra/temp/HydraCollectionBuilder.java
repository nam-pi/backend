package eu.nampi.backend.model.hydra.temp;

import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.WhereBuilder;
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
  public ParameterMapper mapper;
  public WhereBuilder countWhere = new WhereBuilder();
  protected QueryParameters params;
  private Resource orderByVar;

  public HydraCollectionBuilder(JenaService jenaService, String baseUri, Resource mainType,
      Resource orderByVar, QueryParameters params, boolean includeTextFilter,
      boolean optionalLabel) {
    super(jenaService, baseUri, mainType, optionalLabel);
    this.mapper = new ParameterMapper(baseUri, root, model);
    this.orderByVar = orderByVar;
    this.params = params;

    // Set up manages node
    Resource manages = ResourceFactory.createResource();
    this.model
        .add(root, Hydra.manages, manages)
        .add(manages, Hydra.object, mainType);

    // Add text filter
    params.getText().filter(text -> includeTextFilter).ifPresent(text -> {
      Expr matchText = ef.regex(VAR_LABEL, text, "i");
      dataSelect.addFilter(matchText);
      if (optionalLabel) {
        countWhere.addOptional(VAR_MAIN, RDFS.label, VAR_LABEL).addFilter(matchText);
      } else {
        countWhere.addWhere(VAR_MAIN, RDFS.label, VAR_LABEL).addFilter(matchText);
      }
    });

    // Add type filter
    params.getType().map(ResourceFactory::createResource).ifPresent(res -> {
      dataSelect.addWhere(VAR_MAIN, RDF.type, res);
      countWhere.addWhere(VAR_MAIN, RDF.type, res);
    });
  }

  public HydraCollectionBuilder(JenaService jenaService, String baseUri, Resource mainType,
      Resource orderByVar, QueryParameters params) {
    this(jenaService, baseUri, mainType, orderByVar, params, true, false);
  }

  @Override
  public void build(BiFunction<Model, QuerySolution, RDFNode> rowToNode) {
    // Count all possible matches
    Integer totalItems = count();

    // Set up order by, offset and limit using the order-by-clauses
    this.params
        .getOrderByClauses()
        .appendAllTo(dataSelect)
        .addOrderBy(VAR_MAIN)
        .setOffset(params.getOffset())
        .setLimit(params.getLimit());

    // Setup the root hydra collection
    this.model
        .add(this.root, RDF.type, Hydra.Collection)
        .addLiteral(root, Hydra.totalItems, ResourceFactory.createTypedLiteral(totalItems));

    // Query the data using the jena service and add the content provided by the row mapper function
    // to the model
    jenaService.select(dataSelect, row -> this.model
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

  private Integer count() {
    WhereBuilder count = new WhereBuilder()
        .addWhere(VAR_MAIN, RDF.type, mainType)
        .addWhere(countWhere);
    return jenaService.count(count);
  }
}
