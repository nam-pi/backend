package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.utils.Serializer;
import eu.nampi.backend.utils.UrlBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;

@Repository
public class EntrypointRepository {

  @Autowired
  Serializer serializer;

  @Autowired
  UrlBuilder urlBuilder;

  @Autowired
  UserRepository userRepository;

  public String get(Lang lang) {
    Model model = ModelFactory.createDefaultModel()
        .setNsPrefix("api", Api.getURI())
        .setNsPrefix("rdfs", RDFS.getURI())
        .setNsPrefix("core", Core.getURI())
        .setNsPrefix("rdf", RDF.getURI());

    Resource ep = ResourceFactory.createResource(urlBuilder.endpointUri());
    model
        .add(ep, RDF.type, Api.entrypoint)
        .add(ep, Hydra.title, "The NAMPI API");

    Resource acts = ResourceFactory.createResource(urlBuilder.endpointUri("acts"));
    Resource actsBnode = ResourceFactory.createResource();
    model
        .add(ep, Hydra.collection, acts)
        .add(acts, RDF.type, Hydra.collection)
        .add(actsBnode, Hydra.object, Core.act)
        .add(actsBnode, Hydra.property, RDF.type)
        .add(acts, Hydra.manages, actsBnode);

    Resource aspects = ResourceFactory.createResource(urlBuilder.endpointUri("aspects"));
    Resource aspectsBnode = ResourceFactory.createResource();
    model
        .add(ep, Hydra.collection, aspects)
        .add(aspects, RDF.type, Hydra.collection)
        .add(aspectsBnode, Hydra.object, Core.aspect)
        .add(aspectsBnode, Hydra.property, RDF.type)
        .add(aspects, Hydra.manages, aspectsBnode);

    Resource authors = ResourceFactory.createResource(urlBuilder.endpointUri("authors"));
    Resource authorsBnode = ResourceFactory.createResource();
    model
        .add(ep, Hydra.collection, authors)
        .add(authors, RDF.type, Hydra.collection)
        .add(authorsBnode, Hydra.object, Core.author)
        .add(authorsBnode, Hydra.property, RDF.type)
        .add(authors, Hydra.manages, authorsBnode);

    Resource events = ResourceFactory.createResource(urlBuilder.endpointUri("events"));
    Resource eventsBnode = ResourceFactory.createResource();
    model
        .add(ep, Hydra.collection, events)
        .add(events, RDF.type, Hydra.collection)
        .add(eventsBnode, Hydra.object, Core.event)
        .add(eventsBnode, Hydra.property, RDF.type)
        .add(events, Hydra.manages, eventsBnode);

    Resource groups = ResourceFactory.createResource(urlBuilder.endpointUri("groups"));
    Resource groupsBnode = ResourceFactory.createResource();
    model
        .add(ep, Hydra.collection, groups)
        .add(groups, RDF.type, Hydra.collection)
        .add(groupsBnode, Hydra.object, Core.group)
        .add(groupsBnode, Hydra.property, RDF.type)
        .add(groups, Hydra.manages, groupsBnode);

    Resource persons = ResourceFactory.createResource(urlBuilder.endpointUri("persons"));
    Resource personsBnode = ResourceFactory.createResource();
    model
        .add(ep, Hydra.collection, persons)
        .add(persons, RDF.type, Hydra.collection)
        .add(personsBnode, Hydra.object, Core.person)
        .add(personsBnode, Hydra.property, RDF.type)
        .add(persons, Hydra.manages, personsBnode);

    Resource places = ResourceFactory.createResource(urlBuilder.endpointUri("places"));
    Resource placesBnode = ResourceFactory.createResource();
    model
        .add(ep, Hydra.collection, places)
        .add(places, RDF.type, Hydra.collection)
        .add(placesBnode, Hydra.object, Core.place)
        .add(placesBnode, Hydra.property, RDF.type)
        .add(places, Hydra.manages, placesBnode);

    Resource sources = ResourceFactory.createResource(urlBuilder.endpointUri("sources"));
    Resource sourcesBnode = ResourceFactory.createResource();
    model
        .add(ep, Hydra.collection, sources)
        .add(sources, RDF.type, Hydra.collection)
        .add(sourcesBnode, Hydra.object, Core.source)
        .add(sourcesBnode, Hydra.property, RDF.type)
        .add(sources, Hydra.manages, sourcesBnode);

    Resource types = ResourceFactory.createResource(urlBuilder.endpointUri("types"));
    Resource typesBnode = ResourceFactory.createResource();
    model
        .add(ep, Hydra.collection, types)
        .add(types, RDF.type, Hydra.collection)
        .add(typesBnode, Hydra.object, RDFS.Resource)
        .add(typesBnode, Hydra.property, RDF.type)
        .add(types, Hydra.manages, typesBnode);

    userRepository.getCurrentUser().ifPresent(u -> {
      Property user = ResourceFactory.createProperty(urlBuilder.endpointUri("user"));
      model.add(ep, user, Api.user);
    });

    return serializer.serialize(model, lang,
        ResourceFactory.createResource(urlBuilder.endpointUri()));
  }
}
