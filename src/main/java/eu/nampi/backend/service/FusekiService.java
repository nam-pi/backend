package eu.nampi.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.update.UpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import eu.nampi.backend.model.hydra.InterfaceHydraBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FusekiService implements JenaService {

  private RDFConnectionRemoteBuilder dataBuilder;

  private RDFConnectionRemoteBuilder infCacheBuilder;

  @Value("${nampi.core-owl-url}")
  private String coreOwlUrl;

  @Value("${nampi.other-owl-urls}")
  private List<String> otherOwlUrls;

  public FusekiService(RDFConnectionRemoteBuilder dataBuilder,
      RDFConnectionRemoteBuilder infCacheBuilder) {
    this.dataBuilder = dataBuilder;
    this.infCacheBuilder = infCacheBuilder;
  }

  @Override
  public Model construct(ConstructBuilder constructBuilder) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) infCacheBuilder.build()) {
      String query = constructBuilder.buildString();
      log.debug(query);
      return conn.queryConstruct(query);
    }
  }

  @Override
  public Model construct(InterfaceHydraBuilder hydraBuilder) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) infCacheBuilder.build()) {
      String query = hydraBuilder.buildString();
      log.debug(query);
      return conn.queryConstruct(query);
    }
  }

  @Override
  public void initInfCache() {
    List<String> owls = new ArrayList<>();
    owls.add(coreOwlUrl);
    owls.addAll(otherOwlUrls);
    Model infCacheModel = ModelFactory.createDefaultModel();
    for (String url : owls) {
      infCacheModel.read(url);
    }
    Model dataModel = ModelFactory.createDefaultModel();
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) dataBuilder.build()) {
      dataModel = conn.queryConstruct("CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}");
    }
    infCacheModel.add(dataModel);
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) infCacheBuilder.build()) {
      conn.update("DELETE {?s ?p ?o } WHERE {?s ?p ?o }");
      conn.put(infCacheModel);
    }
  }

  @Override
  public void select(SelectBuilder selectBuilder, Consumer<QuerySolution> rowAction) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) infCacheBuilder.build()) {
      String query = selectBuilder.buildString();
      log.debug(query);
      conn.querySelect(query, rowAction);
    }
  }

  @Override
  public void update(UpdateBuilder updateBuilder) {
    UpdateRequest request = updateBuilder.buildRequest();
    log.debug(request.toString());
    // Process update in both data and infCache datasets so the data is kept in sync
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) dataBuilder.build()) {
      conn.update(request);
    }
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) infCacheBuilder.build()) {
      conn.update(request);
    }
  }

}
