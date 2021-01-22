package eu.nampi.backend.configuration;

import java.util.List;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RdfConfig {

  @Value("${nampi.core-owl-url}")
  private String coreOwlUrl;

  @Value("${nampi.other-owl-urls}")
  private List<String> otherOwlUrls;

  @Value("${nampi.triple-store-url}")
  private String tripleStoreUrl;

  private OntModel readOntology() {
    OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
    model.read(coreOwlUrl);
    for (String otherUrl : otherOwlUrls) {
      model.read(otherUrl);
    }
    return model;
  }

  @Bean
  public Reasoner getDefaultReasoner() {
    OntModel model = readOntology();
    Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
    reasoner = reasoner.bindSchema(model);
    return reasoner;
  }

  @Bean
  public RDFConnectionRemoteBuilder getConnectionBuilder() {
    return RDFConnectionFuseki.create().destination(tripleStoreUrl);
  }

}
