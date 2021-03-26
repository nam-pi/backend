package eu.nampi.backend.repository;

import java.io.StringWriter;
import java.util.Optional;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractHydraRepository {

  @Value("${nampi.default-limit}")
  int defaultLimit;

  private static final int NO_OFFSET = 0;

  @Autowired
  JenaService jenaService;

  protected String serialize(Model model, Lang lang) {
    StringWriter writer = new StringWriter();
    RDFDataMgr.write(writer, model, lang);
    return writer.toString();
  }

  protected int calculateOffset(Optional<Integer> limit, Optional<Integer> page, Optional<Integer> pageIndex) {
    int effectiveLimit = calculateLimit(limit);
    return Optional.ofNullable(page.orElseGet(() -> pageIndex.orElse(null)))
        .map(p -> p * effectiveLimit - effectiveLimit).orElse(NO_OFFSET);
  }

  protected int calculateLimit(Optional<Integer> limit) {
    return limit.orElse(defaultLimit);
  }

  protected ConstructBuilder getHydraCollectionBuilder(QueryParameters params, WhereBuilder whereClause,
      String memberVar, Property orderByTemplateMappingProperty) {
    ConstructBuilder builder = new ConstructBuilder();
    ExprFactory exprF = builder.getExprFactory();
    try {
      // @formatter:off
      SelectBuilder countSelect = new SelectBuilder()
        .addVar("COUNT(*)", "?count")
        .addWhere(whereClause);
      SelectBuilder dataSelect = new SelectBuilder()
        .addVar("*")
        .addWhere(whereClause);
      params
        .getOrderByClauses()
        .appendAllTo(dataSelect);
      dataSelect
        .addOrderBy(memberVar)
        .setLimit(params.getLimit())
        .setOffset(params.getOffset());
      SelectBuilder searchSelect = new SelectBuilder()
        .addVar("*")
        .addBind("bnode()", "?search")
        .addBind(exprF.concat(params.getRelativePath(), "{?pageIndex,limit,offset,orderBy}"), "?template")
        .addBind("bnode()", "?pageIndexMapping")
        .addBind("bnode()", "?limitMapping")
        .addBind("bnode()", "?offsetMapping")
        .addBind("bnode()", "?orderByMapping");
      builder
        .addWhere(new WhereBuilder().addUnion(countSelect).addUnion(dataSelect).addUnion(searchSelect))
        .addPrefix("api", Api.getURI())
        .addPrefix("core", Core.getURI())
        .addPrefix("hydra", Hydra.getURI())
        .addPrefix("rdf", RDF.getURI())
        .addPrefix("rdfs", RDFS.getURI())
        .addPrefix("xsd", XSD.getURI())
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
        .addBind(exprF.asExpr(params.getBaseUrl()), "?baseUrl")
        .addBind(exprF.asExpr(params.getLimit()), "?limit")
        .addBind(exprF.asExpr(params.getOffset()), "?offset")
        .addBind(exprF.asExpr(params.getRelativePath()), "?path")
        .addBind(exprF.asExpr(params.isCustomLimit()), "?customMeta")
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
      addHydraTemplateVariable(builder, "?search", "?orderByMapping", orderByTemplateMappingProperty, false, "orderBy");
      addHydraTemplateVariable(builder, "?search", "?pageIndexMapping", Hydra.pageIndex, false, "pageIndex");
      // @formatter:off
    } catch (ParseException e) {
      log.error(e.getMessage());
    }
    return builder;
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
