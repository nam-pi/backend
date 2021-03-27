package eu.nampi.backend.model;

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
public class HydraBuilder {
  private ExprFactory exprF;
  private QueryParameters params;
  private WhereBuilder mainWhere = new WhereBuilder();
  private String memberVar;
  private ConstructBuilder builder = new ConstructBuilder();
  private Property orderByTemplateMappingProperty;

  public HydraBuilder(QueryParameters params, String memberVar, Property orderByTemplateMappingProperty) {
    this.exprF = this.builder.getExprFactory();
    this.params = params;
    this.memberVar = memberVar;
    this.orderByTemplateMappingProperty = orderByTemplateMappingProperty;
  }

  public HydraBuilder addBind(String expression, Object var) {
    this.mainWhere.addBind(this.mainWhere.makeExpr(expression), var);
    return this;
  }

  public HydraBuilder addConstruct(FrontsTriple t) {
    this.builder.addConstruct(t);
    return this;
  }

  public HydraBuilder addConstruct(Triple t) {
    this.builder.addConstruct(t);
    return this;
  }

  public HydraBuilder addConstruct(Object s, Object p, Object o) {
    this.builder.addConstruct(s, p, o);
    return this;
  }

  public HydraBuilder addConstruct(Object p, Object o) {
    return addConstruct(this.memberVar, p, o);
  }

  public HydraBuilder addOptional(WhereBuilder whereClause) {
    this.mainWhere.addOptional(whereClause);
    return this;
  }

  public HydraBuilder addWhere(WhereBuilder whereClause) {
    this.mainWhere.addWhere(whereClause);
    return this;
  }

  public HydraBuilder addWhere(Object s, Object p, Object o) {
    this.mainWhere.addWhere(s, p, o);
    return this;
  }

  public HydraBuilder addWhere(Object p, Object o) {
    return addWhere(this.memberVar, p, o);
  }

  public Query build() {
    params.getType().ifPresent(t -> {
      String iri = "<" + t + ">";
      this.mainWhere.addWhere(this.memberVar, RDF.type, iri);
      this.builder.addConstruct(this.memberVar, RDF.type, iri);
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
      dataSelect.addOrderBy(this.memberVar)
        .setLimit(this.params.getLimit())
        .setOffset(this.params.getOffset());
      SelectBuilder searchSelect = new SelectBuilder()
        .addVar("*")
        .addBind("bnode()", "?search")
        .addBind(this.exprF.concat(this.params.getRelativePath(), "{?pageIndex,limit,offset,orderBy,type}"), "?template")
        .addBind("bnode()", "?pageIndexMapping")
        .addBind("bnode()", "?limitMapping")
        .addBind("bnode()", "?offsetMapping")
        .addBind("bnode()", "?orderByMapping")
        .addBind("bnode()", "?typeMapping");
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
        .addConstruct("?col", Hydra.member, memberVar)
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
        .addBind(builder.makeExpr("?currentNumber + 1"), "?nextNumber")
        .addBind(builder.makeExpr("?currentNumber - 1"), "?prevNumber")
        .addBind(builder.makeExpr("concat(?path, ?limitQuery, '?page=', xsd:string(?lastNumber))"), "?last")
        .addBind(builder.makeExpr("concat(?path, ?limitQuery, '?page=1')"), "?first")
        .addBind(builder.makeExpr("if(?customMeta, concat('&limit=', xsd:string(?limit)), '')"), "?limitQuery")
        .addBind(builder.makeExpr("if(?nextNumber < ?lastNumber, concat(?path, ?limitQuery, '?page=', xsd:string(?nextNumber)), 1+'')"), "?next")
        .addBind(builder.makeExpr("if(?prevNumber > 0, concat(?path, ?limitQuery, '?page=', xsd:string(?prevNumber)), 1+'')"), "?prev")
        .addBind(builder.makeExpr("iri(concat(?baseUrl, ?limitQuery, '?page=', xsd:string(?currentNumber)))"), "?view")
        .addBind(builder.makeExpr("xsd:integer(floor(?count / ?limit))"), "?lastNumber")
        .addBind(builder.makeExpr("xsd:integer(floor(?offset / ?limit + 1))"), "?currentNumber");
      addHydraTemplateVariable(builder, "?search", "?limitMapping", Hydra.limit, false, "limit");
      addHydraTemplateVariable(builder, "?search", "?offsetMapping", Hydra.offset, false, "offset");
      addHydraTemplateVariable(builder, "?search", "?orderByMapping", this.orderByTemplateMappingProperty, false, "orderBy");
      addHydraTemplateVariable(builder, "?search", "?pageIndexMapping", Hydra.pageIndex, false, "pageIndex");
      addHydraTemplateVariable(builder, "?search", "?typeMapping", Api.type, false, "type");
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

  private void addHydraTemplateVariable(ConstructBuilder builder, String parent, String self, Property property,
      boolean required, String variable) {
    // @formatter:off
    builder
      .addConstruct(self, RDF.type, Hydra.IriTemplateMapping)
      .addConstruct(self, Hydra.property, property)
      .addConstruct(self, Hydra.required, required ? "true" : "false")
      .addConstruct(self, Hydra.variable, variable)
      .addConstruct(parent, Hydra.mapping, self);
    // @formatter:on
  }
}
