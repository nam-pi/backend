package eu.nampi.backend.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HydraCollectionBuilder {

  public static final String MAIN_LABEL = "?label";

  public static final String MAIN_SUBJ = "?main";

  private ConstructBuilder builder = new ConstructBuilder();

  private ExprFactory exprF;

  private WhereBuilder mainWhere = new WhereBuilder();

  private Property mainType;

  private Property orderByTemplateMappingProperty;

  private QueryParameters params;

  private List<String> templateVariables = new ArrayList<>();

  public HydraCollectionBuilder(QueryParameters params, Property mainType, Property orderByTemplateMappingProperty) {
    this(params, mainType, orderByTemplateMappingProperty, false);
  }

  public HydraCollectionBuilder(QueryParameters params, Property mainType, Property orderByTemplateMappingProperty,
      boolean disableTypeFilter) {
    this.exprF = this.builder.getExprFactory();
    this.mainType = mainType;
    this.orderByTemplateMappingProperty = orderByTemplateMappingProperty;
    this.params = params;

    this.mainWhere.addWhere(MAIN_SUBJ, RDF.type, this.mainType).addWhere(MAIN_SUBJ, RDFS.label, MAIN_LABEL);
    this.builder.addConstruct(MAIN_SUBJ, RDF.type, this.mainType).addConstruct(MAIN_SUBJ, RDFS.label, MAIN_LABEL);

    addSearchVariable("limit", Hydra.limit, false);
    addSearchVariable("offset", Hydra.offset, false);
    addSearchVariable("pageIndex", Hydra.pageIndex, false);
    addSearchVariable("orderBy", this.orderByTemplateMappingProperty, false);
    if (!disableTypeFilter) {
      addSearchVariable("type", Api.typeVar, false);
    }
  }

  public HydraCollectionBuilder addBind(String expression, Object var) {
    this.mainWhere.addBind(this.mainWhere.makeExpr(expression), var);
    return this;
  }

  public HydraCollectionBuilder addConstruct(FrontsTriple t) {
    this.builder.addConstruct(t);
    return this;
  }

  public HydraCollectionBuilder addConstruct(Triple t) {
    this.builder.addConstruct(t);
    return this;
  }

  public HydraCollectionBuilder addConstruct(Object s, Object p, Object o) {
    this.builder.addConstruct(s, p, o);
    return this;
  }

  public HydraCollectionBuilder addMainConstruct(Object p, Object o) {
    return addConstruct(MAIN_SUBJ, p, o);
  }

  public HydraCollectionBuilder addMainWhere(Object p, Object o) {
    return addWhere(MAIN_SUBJ, p, o);
  }

  public HydraCollectionBuilder addOptional(WhereBuilder whereClause) {
    this.mainWhere.addOptional(whereClause);
    return this;
  }

  public HydraCollectionBuilder addSearchVariable(String name, Property property, boolean required) {
    this.templateVariables.add(name);
    String templateMapping = mappingVar(name);
    System.out.println(name);
    // @formatter:off
    this.builder
      .addConstruct(templateMapping, RDF.type, Hydra.IriTemplateMapping)
      .addConstruct(templateMapping, Hydra.property, property)
      .addConstruct(templateMapping, Hydra.required, required ? "true" : "false")
      .addConstruct(templateMapping, Hydra.variable, name)
      .addConstruct("?search", Hydra.mapping, templateMapping);
    // @formatter:on
    return this;
  }

  public HydraCollectionBuilder addWhere(WhereBuilder whereClause) {
    this.mainWhere.addWhere(whereClause);
    return this;
  }

  public HydraCollectionBuilder addWhere(Object s, Object p, Object o) {
    this.mainWhere.addWhere(s, p, o);
    return this;
  }

  public Query build() {
    params.getType().ifPresent(t -> {
      String iri = "<" + t + ">";
      this.mainWhere.addWhere(MAIN_SUBJ, RDF.type, iri);
      this.builder.addConstruct(MAIN_SUBJ, RDF.type, iri);
    });
    try {
      // @formatter:off
      SelectBuilder countSelect = new SelectBuilder()
        .addVar("COUNT(*)", "?count")
        .addWhere(this.mainWhere);
      SelectBuilder dataSelect = new SelectBuilder()
        .addVar("*")
        .addWhere(this.mainWhere);
      params
        .getOrderByClauses()
        .appendAllTo(dataSelect);
      dataSelect.addOrderBy(MAIN_SUBJ)
        .setLimit(this.params.getLimit())
        .setOffset(this.params.getOffset());
      SelectBuilder searchSelect = new SelectBuilder()
        .addVar("*")
        .addBind("bnode()", "?search")
        .addBind(this.exprF.concat(this.params.getRelativePath(), "{?" + this.templateVariables.stream().collect(Collectors.joining(",")) + "}"), "?template");
      for (String string : templateVariables) {
        searchSelect.addBind("bnode()", mappingVar(string));
      }
      this.builder
        .addPrefix("api", Api.getURI())
        .addPrefix("core", Core.getURI())
        .addPrefix("hydra", Hydra.getURI())
        .addPrefix("rdf", RDF.getURI())
        .addPrefix("rdfs", RDFS.getURI())
        .addPrefix("xsd", XSD.getURI())
        .addWhere(new WhereBuilder()
          .addUnion(countSelect)
          .addUnion(dataSelect)
          .addUnion(searchSelect))
        .addConstruct("?col", Hydra.member, MAIN_SUBJ)
        .addConstruct("?col", Hydra.search, "?search")
        .addConstruct("?col", Hydra.totalItems, "?count")
        .addConstruct("?col", Hydra.view, "?view")
        .addConstruct("?col", RDF.type, Hydra.Collection)
        .addConstruct("?search", Hydra.template, "?template")
        .addConstruct("?search", Hydra.variableRepresentation, Hydra.BasicRepresentation)
        .addConstruct("?search", RDF.type, Hydra.IriTemplate)
        .addConstruct("?view", Hydra.first, "?first")
        .addConstruct("?view", Hydra.last, "?last")
        .addConstruct("?view", Hydra.next, "?next")
        .addConstruct("?view", Hydra.previous, "?prev")
        .addConstruct("?view", RDF.type, Hydra.PartialCollectionView)
        .addBind(exprF.asExpr(this.params.getBaseUrl()), "?baseUrl")
        .addBind(exprF.asExpr(this.params.getLimit()), "?limit")
        .addBind(exprF.asExpr(this.params.getOffset()), "?offset")
        .addBind(exprF.asExpr(this.params.getRelativePath()), "?path")
        .addBind(exprF.asExpr(this.params.isCustomLimit()), "?customMeta")
        .addBind(exprF.iri("?baseUrl"), "?col")
        .addBind(builder.makeExpr("xsd:integer(ceil(?offset / ?limit + 1))"), "?currentNumber")
        .addBind(builder.makeExpr("?currentNumber + 1"), "?nextNumber")
        .addBind(builder.makeExpr("?currentNumber - 1"), "?prevNumber")
        .addBind(builder.makeExpr("xsd:integer(ceil(?count / ?limit))"), "?lastNumber")
        .addBind(builder.makeExpr("if(?customMeta, concat('&limit=', xsd:string(?limit)), '')"), "?limitQuery")
        .addBind(builder.makeExpr("concat(?path, ?limitQuery, '?page=1')"), "?first")
        .addBind(builder.makeExpr("concat(?path, ?limitQuery, '?page=', xsd:string(?lastNumber))"), "?last")
        .addBind(builder.makeExpr("if(?nextNumber <= ?lastNumber, concat(?path, ?limitQuery, '?page=', xsd:string(?nextNumber)), 1+'')"), "?next")
        .addBind(builder.makeExpr("if(?prevNumber > 0, concat(?path, ?limitQuery, '?page=', xsd:string(?prevNumber)), 1+'')"), "?prev")
        .addBind(builder.makeExpr("iri(concat(?baseUrl, ?limitQuery, '?page=', xsd:string(?currentNumber)))"), "?view")
        ;
      // @formatter:on
      return this.builder.build();
    } catch (ParseException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  public String buildString() {
    return build().toString();
  }

  private String mappingVar(String var) {
    return pad(var) + "Mapping";
  }

  private String pad(String var) {
    return var.startsWith("?") ? var : "?" + var;
  }

}
