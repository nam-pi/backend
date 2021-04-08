package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.Vocab;

@Repository
public class EntrypointRepository extends AbstractHydraRepository {

  public String get(Lang lang) {
    Model model = ModelFactory.createDefaultModel();
    model.setNsPrefix("vocab", Vocab.getURI());
    Resource ep = ResourceFactory.createResource(endpointUri());
    model.add(ep, RDF.type, Vocab.entrypoint);
    model.add(ep, Hydra.title, "The NAMPI API");
    addCollection(model, ep, "events");
    addCollection(model, ep, "persons");
    return serialize(model, lang, ResourceFactory.createResource(endpointUri()));
  }

  private void addCollection(Model model, Resource ep, String endpointName) {
    Resource res = ResourceFactory.createResource(endpointUri(endpointName));
    model.add(res, RDF.type, Hydra.Collection);
    model.add(ep, Hydra.collection, res);
  }

}
