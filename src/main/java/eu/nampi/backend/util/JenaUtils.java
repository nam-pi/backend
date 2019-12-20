package eu.nampi.backend.util;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.util.FileManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JenaUtils {

  @Value("${nampi.core-owl-url}")
  private String coreOwlUrl;

  @Value("${nampi.triple-store-url}")
  private String tripleStoreUrl;

  private OntModel readOntology(String url) {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    try {
      InputStream in = FileManager.get().open(url);
      model.read(in, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return model;
  }

  private QueryExecution createQueryExecution(String query, Dataset dataset, boolean useInference) {
    Model defaultModel = dataset.getDefaultModel();
    if (useInference) {
      OntModel schema = readOntology(coreOwlUrl);
      Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
      reasoner = reasoner.bindSchema(schema);
      InfModel infModel = ModelFactory.createInfModel(reasoner, defaultModel);
      return QueryExecutionFactory.create(query, infModel);
    } else {
      return QueryExecutionFactory.create(query, defaultModel);
    }
  }

  public Model constructCore(String query, boolean useInference) {
    try (RDFConnection conn = RDFConnectionFactory.connect(tripleStoreUrl)) {
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

  public ResultSet selectCore(String query, boolean useInference) {
    try (RDFConnection conn = RDFConnectionFactory.connect(tripleStoreUrl)) {
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

  public void writeModel(Model model, Lang lang, HttpServletResponse response) {
    try {
      RDFDataMgr.write(response.getOutputStream(), model, lang);
    } catch (IOException e) {
      response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
      e.printStackTrace();
    }
  }
}
