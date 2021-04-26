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

    Resource aspects = ResourceFactory.createResource(endpointUri("aspects"));
    model.add(ep, Hydra.collection, aspects);
    model.add(aspects, RDF.type, Hydra.collection);
    Resource aspectsBnode = ResourceFactory.createResource();
    model.add(aspectsBnode, Hydra.object, Core.aspect);
    model.add(aspectsBnode, Hydra.property, RDF.type);
    model.add(aspects, Hydra.manages, aspectsBnode);

    Resource authors = ResourceFactory.createResource(endpointUri("authors"));
    model.add(ep, Hydra.collection, authors);
    model.add(authors, RDF.type, Hydra.collection);
    Resource authorsBnode = ResourceFactory.createResource();
    model.add(authorsBnode, Hydra.object, Core.author);
    model.add(authorsBnode, Hydra.property, RDF.type);
    model.add(authors, Hydra.manages, authorsBnode);

    Resource events = ResourceFactory.createResource(endpointUri("events"));
    model.add(ep, Hydra.collection, events);
    model.add(events, RDF.type, Hydra.collection);
    Resource eventsBnode = ResourceFactory.createResource();
    model.add(eventsBnode, Hydra.object, Core.event);
    model.add(eventsBnode, Hydra.property, RDF.type);
    model.add(events, Hydra.manages, eventsBnode);

    Resource groups = ResourceFactory.createResource(endpointUri("groups"));
    model.add(ep, Hydra.collection, groups);
    model.add(groups, RDF.type, Hydra.collection);
    Resource groupsBnode = ResourceFactory.createResource();
    model.add(groupsBnode, Hydra.object, Core.group);
    model.add(groupsBnode, Hydra.property, RDF.type);
    model.add(groups, Hydra.manages, groupsBnode);

    Resource persons = ResourceFactory.createResource(endpointUri("persons"));
    model.add(ep, Hydra.collection, persons);
    model.add(persons, RDF.type, Hydra.collection);
    Resource personsBnode = ResourceFactory.createResource();
    model.add(personsBnode, Hydra.object, Core.person);
    model.add(personsBnode, Hydra.property, RDF.type);
    model.add(persons, Hydra.manages, personsBnode);

    Resource places = ResourceFactory.createResource(endpointUri("places"));
    model.add(ep, Hydra.collection, places);
    model.add(places, RDF.type, Hydra.collection);
    Resource placesBnode = ResourceFactory.createResource();
    model.add(placesBnode, Hydra.object, Core.place);
    model.add(placesBnode, Hydra.property, RDF.type);
    model.add(places, Hydra.manages, placesBnode);

    userRepository.getCurrentUser().ifPresent(u -> {
      Property user = ResourceFactory.createProperty(endpointUri("user"));
      model.add(ep, user, Doc.user);
    });

    return serialize(model, lang, ResourceFactory.createResource(endpointUri()));
  }

}
