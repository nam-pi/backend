package eu.nampi.backend.repository;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.service.JenaService;

@Repository
public class OwlRepository {

  private static final String GRAPH_URI = "https://purl.org/nampi/graph/owlinf";

  @Autowired
  private JenaService jenaService;

  public void deleteAll() {
    jenaService.clearGraph(GRAPH_URI);
  }

  public void storeOwls(List<String> owlUrls) {
    Model model = ModelFactory.createDefaultModel();
    for (String url : owlUrls) {
      model.read(url);
    }
    jenaService.replaceGraph(GRAPH_URI, model);
  }

}
