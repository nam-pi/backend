package eu.nampi.backend.repository;

import javax.servlet.http.HttpServletRequest;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRdfRepository {

  @Autowired
  JenaService jenaService;

  private static HttpServletRequest getRequest() {
    return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
  }

  protected static ConstructBuilder getHydraCollectionBuilder(WhereBuilder whereClause,
      String memberVar, String orderBy, int limit, int offset) {
    try {
      SelectBuilder countSelect =
          new SelectBuilder().addVar("COUNT(*)", "?count").addWhere(whereClause);
      SelectBuilder dataSelect = new SelectBuilder().addVar("*").addWhere(whereClause)
          .addOrderBy(orderBy).setLimit(limit).setOffset(offset);
      WhereBuilder combinedWhere = new WhereBuilder().addUnion(countSelect).addUnion(dataSelect);
      ConstructBuilder builder = new ConstructBuilder();
      ExprFactory exprF = builder.getExprFactory();
      builder.addPrefix("core", Core.getURI()).addPrefix("rdfs", RDFS.getURI())
          .addPrefix("rdf", RDF.getURI()).addPrefix("hydra", Hydra.getURI())
          .addPrefix("xsd", XSD.getURI()).addConstruct("?col", RDF.type, Hydra.Collection)
          .addConstruct("?col", Hydra.totalItems, "?count")
          .addConstruct("?col", Hydra.member, memberVar).addConstruct("?col", Hydra.view, "?view")
          .addConstruct("?view", RDF.type, Hydra.PartialCollectionView)
          .addConstruct("?view", Hydra.first, "?first")
          .addConstruct("?view", Hydra.previous, "?prev").addConstruct("?view", Hydra.next, "?next")
          .addConstruct("?view", Hydra.last, "?last").addWhere(combinedWhere)
          .addBind(exprF.asExpr(offset), "?offset").addBind(exprF.asExpr(limit), "?limit")
          .addBind(exprF.asExpr(getRequest().getRequestURL().toString()), "?baseUrl")
          .addBind(exprF.iri("?baseUrl"), "?col")
          .addBind(builder.makeExpr("xsd:integer(floor(?offset / ?limit + 1))"), "?currentNumber")
          .addBind(builder.makeExpr("xsd:integer(floor(?count / ?limit))"), "?lastNumber")
          .addBind(builder.makeExpr("?currentNumber - 1"), "?prevNumber")
          .addBind(builder.makeExpr("?currentNumber + 1"), "?nextNumber")
          .addBind(builder.makeExpr("iri(concat(?baseUrl, '?page=', xsd:string(?currentNumber)))"),
              "?view")
          .addBind(builder.makeExpr("iri(concat(?baseUrl, '?page=1'))"), "?first")
          .addBind(builder.makeExpr(
              "iri(if(?prevNumber > 0, concat(?baseUrl, '?page=', xsd:string(?prevNumber)), 1+''))"),
              "?prev")
          .addBind(builder.makeExpr(
              "iri(if(?nextNumber < ?lastNumber, concat(?baseUrl, '?page=', xsd:string(?nextNumber)), 1+''))"),
              "?next")
          .addBind(builder.makeExpr("iri(concat(?baseUrl, '?page=', xsd:string(?lastNumber)))"),
              "?last");
      return builder;
    } catch (ParseException e) {
      log.error(e.getMessage());
      return null;
    }
  }

}
