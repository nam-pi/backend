package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.SchemaOrg;
import eu.nampi.backend.vocabulary.Vocab;

@Repository
public class DocumentationRepository extends AbstractHydraRepository {

  public String get(Lang lang) {
    Model model = ModelFactory.createDefaultModel();
    model.setNsPrefix("vocab", Vocab.getURI()).setNsPrefix("hydra", Hydra.getURI()).setNsPrefix("rdf", RDF.getURI())
        .setNsPrefix("rdfs", RDFS.getURI()).setNsPrefix("schema", SchemaOrg.getURI());
    Resource doc = ResourceFactory.createResource(Vocab.baseUri);
    model.add(doc, RDF.type, Hydra.ApiDocumentation);
    model.add(doc, Hydra.title, "The NAMPI API");
    model.add(doc, Hydra.description,
        "An API for the prosopographical data of the project 'Nuns and Monks - Prosopograpical Interfaces'");
    model.add(doc, Hydra.entrypoint, endpointUri());
    addCollection(model, doc, Vocab.personCollection, Core.person);
    addCollection(model, doc, Vocab.eventCollection, Core.event);
    addClass(model, doc, Hydra.Resource);
    addClass(model, doc, Hydra.Collection);
    addEntrypoint(model, doc);
    return serialize(model, lang, doc);
  }

  private void addCollection(Model model, Resource doc, Property type, Property managed) {
    model.add(doc, Hydra.supportedClass, type);
    model.add(type, RDF.type, Hydra.Collection);
    model.add(type, RDFS.subClassOf, Hydra.Collection);
    model.add(type, Hydra.title, type.getLocalName());
  }

  private void addClass(Model model, Resource doc, Property type) {
    model.add(doc, Hydra.supportedClass, type);
    model.add(type, RDF.type, Hydra.Class);
    model.add(type, Hydra.title, type.getLocalName());
  }

  private void addEntrypoint(Model model, Resource doc) {
    Property ep = Vocab.entrypoint;
    addClass(model, doc, ep);
    model.add(ep, RDF.type, SchemaOrg.EntryPoint);
  }

}
