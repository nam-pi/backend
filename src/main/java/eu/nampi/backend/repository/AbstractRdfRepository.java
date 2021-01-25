package eu.nampi.backend.repository;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.springframework.beans.factory.annotation.Autowired;
import eu.nampi.backend.model.CollectionMeta;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRdfRepository {

  @Autowired
  JenaService jenaService;

  protected ConstructBuilder getHydraCollectionBuilder(CollectionMeta meta,
      WhereBuilder whereClause, String memberVar, String orderBy) {
    try {
      SelectBuilder countSelect =
          new SelectBuilder().addVar("COUNT(*)", "?count").addWhere(whereClause);
      SelectBuilder dataSelect = new SelectBuilder().addVar("*").addWhere(whereClause)
          .addOrderBy(orderBy).setLimit(meta.getLimit()).setOffset(meta.getOffset());
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
          .addBind(exprF.asExpr(meta.getLimit()), "?limit")
          .addBind(exprF.asExpr(meta.getOffset()), "?offset")
          .addBind(exprF.asExpr(meta.getBaseUrl()), "?baseUrl")
          .addBind(exprF.asExpr(meta.isCustomLimit()), "?customMeta")
          .addBind(exprF.iri("?baseUrl"), "?col")
          .addBind(builder.makeExpr("if(?customMeta, concat('&limit=', xsd:string(?limit)), '')"),
              "?limitQuery")
          .addBind(builder.makeExpr("xsd:integer(floor(?offset / ?limit + 1))"), "?currentNumber")
          .addBind(builder.makeExpr("xsd:integer(floor(?count / ?limit))"), "?lastNumber")
          .addBind(builder.makeExpr("?currentNumber - 1"), "?prevNumber")
          .addBind(builder.makeExpr("?currentNumber + 1"), "?nextNumber")
          .addBind(
              builder.makeExpr(
                  "iri(concat(?baseUrl, ?limitQuery, '?page=', xsd:string(?currentNumber)))"),
              "?view")
          .addBind(builder.makeExpr("iri(concat(?baseUrl, ?limitQuery, '?page=1' ))"), "?first")
          .addBind(builder.makeExpr(
              "iri(if(?prevNumber > 0, concat(?baseUrl, ?limitQuery, '?page=', xsd:string(?prevNumber)), 1+''))"),
              "?prev")
          .addBind(builder.makeExpr(
              "iri(if(?nextNumber < ?lastNumber, concat(?baseUrl, ?limitQuery, '?page=', xsd:string(?nextNumber)), 1+''))"),
              "?next")
          .addBind(
              builder.makeExpr(
                  "iri(concat(?baseUrl, ?limitQuery, '?page=', xsd:string(?lastNumber)))"),
              "?last");
      return builder;
    } catch (ParseException e) {
      log.error(e.getMessage());
      return null;
    }
  }

}
