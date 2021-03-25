package eu.nampi.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.springframework.beans.factory.annotation.Value;

public class FusekiService implements JenaService {

  private RDFConnectionRemoteBuilder dataBuilder;

  private RDFConnectionRemoteBuilder infCacheBuilder;

  @Value("${nampi.core-owl-url}")
  private String coreOwlUrl;

  @Value("${nampi.other-owl-urls}")
  private List<String> otherOwlUrls;

  public FusekiService(RDFConnectionRemoteBuilder dataBuilder, RDFConnectionRemoteBuilder infCacheBuilder) {
    this.dataBuilder = dataBuilder;
    this.infCacheBuilder = infCacheBuilder;
  }

  @Override
  public Model construct(ConstructBuilder constructBuilder) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) infCacheBuilder.build()) {
      return conn.queryConstruct(constructBuilder.build());
    }
  }

  @Override
  public void initInfCache() {
    List<String> owls = new ArrayList<>();
    owls.add(coreOwlUrl);
    owls.addAll(otherOwlUrls);
    Model dataModel = ModelFactory.createDefaultModel();
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) dataBuilder.build()) {
      dataModel = conn.queryConstruct("CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}");
    }
    Model infCacheModel = ModelFactory.createDefaultModel();
    for (String url : owls) {
      infCacheModel.read(url);
    }
    infCacheModel.add(dataModel);
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) infCacheBuilder.build()) {
      conn.put(infCacheModel);
    }
  }

}
