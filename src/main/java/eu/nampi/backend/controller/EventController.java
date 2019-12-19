package eu.nampi.backend.controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

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
import org.apache.jena.util.FileManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventController {

  OntModel readOntology(String url) {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    try {
      InputStream in = FileManager.get().open(url);
      model.read(in, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return model;
  }

  @GetMapping("/events")
  public Collection<String> getEvents() {
    Collection<String> results = new ArrayList<>();
    OntModel schema = readOntology("https://raw.githubusercontent.com/nam-pi/ontologies/master/core.owl");
    try (RDFConnection conn = RDFConnectionFactory.connect("http://fuseki:3030/nampi")) {
      Dataset dataset = conn.fetchDataset();
      dataset.begin();

      try {
        Model data = dataset.getDefaultModel();
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(schema);
        InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
        String query = """
                PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>

                SELECT *
                WHERE {
                  ?event rdf:type <https://purl.org/nampi/owl/core#event>.
                }
            """;
        QueryExecution qexec = QueryExecutionFactory.create(query, infmodel);
        ResultSet result = qexec.execSelect();
        while (result.hasNext()) {
          results.add("http://localhost:4000" + result.next().get("event").asResource().getLocalName());
        }

      } finally {
        dataset.end();
      }
    }
    return results;
  }

}
