package eu.nampi.backend.service;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.reasoner.Reasoner;
import org.springframework.beans.factory.annotation.Autowired;

public class FusekiService implements JenaService {

  @Autowired
  private Reasoner reasoner;

  @Autowired
  private RDFConnectionRemoteBuilder connectionBuilder;

  private QueryExecution createQueryExecution(String query, Dataset dataset, boolean useInference) {
    Model defaultModel = dataset.getDefaultModel();
    if (useInference) {
      InfModel infModel = ModelFactory.createInfModel(reasoner, defaultModel);
      return QueryExecutionFactory.create(query, infModel);
    } else {
      return QueryExecutionFactory.create(query, defaultModel);
    }
  }

  public Model construct(String query, boolean useInference) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) connectionBuilder.build()) {
      Dataset dataset = conn.fetchDataset();
      dataset.begin();
      try {
        QueryExecution qexec = createQueryExecution(query, dataset, useInference);
        return qexec.execConstruct();
      } finally {
        dataset.end();
      }
    }
  }

  public ResultSet select(String query, boolean useInference) {
    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) connectionBuilder.build()) {
      Dataset dataset = conn.fetchDataset();
      dataset.begin();
      try {
        QueryExecution qexec = createQueryExecution(query, dataset, useInference);
        return qexec.execSelect();
      } finally {
        dataset.end();
      }
    }
  }

}
