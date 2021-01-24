package eu.nampi.backend.repository;

import javax.servlet.http.HttpServletRequest;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
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

  protected static ConstructBuilder getHydraCollectionBuilder(String colVar, int limit,
      int offset) {
    try {
      return new ConstructBuilder().addPrefix("core", Core.uri).addPrefix("rdfs", RDFS.uri)
          .addPrefix("rdf", RDF.uri).addPrefix("hydra", Hydra.uri)
          .addConstruct("?col", RDF.type, Hydra.Collection)
          .addConstruct("?col", Hydra.member, colVar).addBind(getRequestUrlExpression(), "?col")
          .setLimit(limit).setOffset(offset);
    } catch (ParseException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  private static String getRequestUrl() {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
    String queryString = request.getQueryString();
    if (queryString == null) {
      return requestURL.toString();
    } else {
      return requestURL.append('?').append(queryString).toString();
    }
  }

  protected static String getRequestUrlExpression() {
    return "<" + getRequestUrl() + ">";
  }
}
