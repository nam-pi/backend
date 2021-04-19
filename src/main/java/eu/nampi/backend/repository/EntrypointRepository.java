package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;
import eu.nampi.backend.vocabulary.Hydra;

@Repository
public class EntrypointRepository extends AbstractHydraRepository {

  @Autowired
  UserRepository userRepository;

  public String get(Lang lang) {
    Model model = ModelFactory.createDefaultModel();
    model.setNsPrefix("doc", Doc.getURI());
    Resource ep = ResourceFactory.createResource(endpointUri());
    model.add(ep, RDF.type, Doc.entrypoint);
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

    userRepository.getCurrentUser().ifPresent(u -> {
      Property user = ResourceFactory.createProperty(endpointUri("user"));
      model.add(ep, user, Doc.user);
    });

    return serialize(model, lang, ResourceFactory.createResource(endpointUri()));
  }

}
