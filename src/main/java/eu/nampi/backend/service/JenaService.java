package eu.nampi.backend.service;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
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
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.ResultSetMgr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JenaService {

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

  public void writeToOutStream(Model model, Lang lang, HttpServletResponse response) {
    try {
      RDFDataMgr.write(response.getOutputStream(), model, lang);
    } catch (IOException e) {
      response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
      e.printStackTrace();
    }
  }

  public void writeToOutStream(ResultSet results, Lang lang, HttpServletResponse response) {
    try {
      ResultSetMgr.write(response.getOutputStream(), results, lang);
    } catch (IOException e) {
      response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
      e.printStackTrace();
    }
  }
}
