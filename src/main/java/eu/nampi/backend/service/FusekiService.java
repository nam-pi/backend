package eu.nampi.backend.service;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class FusekiService implements JenaService {

  @Autowired
  private RDFConnectionRemoteBuilder connectionBuilder;

  @Override
  public Model construct(ConstructBuilder constructBuilder) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) connectionBuilder.build()) {
      return conn.queryConstruct(constructBuilder.build());
    }
  }

  @Override
  public void clearGraph(String uri) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) connectionBuilder.build()) {
      conn.update("CLEAR GRAPH <" + uri + ">");
    }
  }

  @Override
  public void replaceGraph(String graphUri, Model model) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) connectionBuilder.build()) {
      conn.put(graphUri, model);
    }
  }
}
