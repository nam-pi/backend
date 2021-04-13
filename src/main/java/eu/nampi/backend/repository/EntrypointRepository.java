package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.vocabulary.Core;
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

    Resource events = ResourceFactory.createResource(endpointUri("events"));
    model.add(ep, Hydra.collection, events);
    model.add(events, RDF.type, Hydra.collection);
    Resource eventsBnode = ResourceFactory.createResource();
    model.add(eventsBnode, Hydra.object, Core.event);
    model.add(eventsBnode, Hydra.property, RDF.type);
    model.add(events, Hydra.manages, eventsBnode);

    Resource persons = ResourceFactory.createResource(endpointUri("persons"));
    model.add(ep, Hydra.collection, persons);
    model.add(persons, RDF.type, Hydra.collection);
    Resource personsBnode = ResourceFactory.createResource();
    model.add(personsBnode, Hydra.object, Core.person);
    model.add(personsBnode, Hydra.property, RDF.type);
    model.add(persons, Hydra.manages, personsBnode);

    return serialize(model, lang, ResourceFactory.createResource(endpointUri()));
  }

  // private void addCollection(Model model, Resource ep, String name, Resource
  // type) {
  // Resource res = ResourceFactory.createResource(endpointUri(endpointName));
  // model.add(res, RDF.type, Hydra.Collection);
  // model.add(ep, Hydra.collection, res);
  // }

}
