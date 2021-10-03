package eu.nampi.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.SchemaOrg;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CacheConfig(cacheNames = "jena")
public class FusekiService implements JenaService {

  @Autowired
  private CacheService cacheService;

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
  public boolean ask(AskBuilder askBuilder) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) infCacheBuilder.build()) {
      String query = askBuilder.buildString();
      log.debug(query);
      return conn.queryAsk(query);
    }
  }

  @Override
  @Cacheable(
      key = "{#whereBuilder.buildString().replaceAll(\"[\\n\\t ]\", \"\"), #distinctVariable.getName()}")
  public int count(WhereBuilder whereBuilder, Node distinctVariable) {
    Node varCount = NodeFactory.createVariable("count");
    SelectBuilder count = new SelectBuilder();
    try {
      count.addVar("count(distinct " + distinctVariable + ")", varCount);
    } catch (ParseException e) {
      log.warn(e.getMessage());
    }
    count.addWhere(whereBuilder);
    AtomicInteger totalItems = new AtomicInteger(0);
    this.select(count, row -> {
      Optional<RDFNode> value = Optional.ofNullable(row.get(varCount.getName()));
      totalItems.set(value.map(RDFNode::asLiteral).map(Literal::getInt).orElse(0));
    });
    return totalItems.get();
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
    cacheService.clear();
  }

  @Override
  public void select(SelectBuilder selectBuilder, Consumer<QuerySolution> rowAction) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) infCacheBuilder.build()) {
      String query = selectBuilder.addPrefix("api", Api.getURI()).addPrefix("core", Core.getURI())
          .addPrefix("hydra", Hydra.getURI()).addPrefix("rdf", RDF.getURI())
          .addPrefix("rdfs", RDFS.getURI())
          .addPrefix("schema", SchemaOrg.getURI()).addPrefix("xsd", XSD.getURI()).buildString();
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
    // Clear cache after each update
    cacheService.clear();
  }
}
