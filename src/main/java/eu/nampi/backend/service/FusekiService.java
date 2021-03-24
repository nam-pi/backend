package eu.nampi.backend.service;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class FusekiService implements JenaService {

  @Autowired
  private RDFConnectionRemoteBuilder connectionBuilder;

  public Model construct(String query) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) connectionBuilder.build()) {
      return conn.queryConstruct(query);
    }
  }

}
