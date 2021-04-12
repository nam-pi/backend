package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.hydra.Class;
import eu.nampi.backend.model.hydra.Collection;
import eu.nampi.backend.model.hydra.SupportedProperty;
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
    Class doc = new Class(Vocab.baseUri, "The NAMPI API documentation", Hydra.ApiDocumentation);
    doc.addDescription(
        "The documentation for NAMPI, an API for the prosopographical data of the project 'Nuns and Monks - Prosopograpical Interfaces'");
    doc.add(Hydra.entrypoint, endpointUri());
    addSupportedClasses(doc);
    addSupportedCollections(doc);
    model.add(doc);
    return serialize(model, lang, doc.base());
  }

  private void addSupportedClasses(Class doc) {
    // Classes
    Class entrypoint = new Class(Vocab.entrypoint, Vocab.entrypoint.getLocalName());
    Class event = new Class(Core.event, Core.event.getLocalName());
    event.addSupportedProperty(new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    event.addSupportedProperty(
        new SupportedProperty(Core.hasSortingDate, Core.date, Core.hasSortingDate.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.takesPlaceNotEarlierThan, Core.date,
        Core.takesPlaceNotEarlierThan.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.takesPlaceNotLaterThan, Core.date,
        Core.takesPlaceNotLaterThan.getLocalName(), true, false, false));
    event.addSupportedProperty(
        new SupportedProperty(Core.takesPlaceOn, Core.date, Core.takesPlaceOn.getLocalName(), true, false, false));
    Class person = new Class(Core.person, Core.person.getLocalName());
    person.addSupportedProperty(new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    doc.add(Hydra.supportedClass, entrypoint, event, person);
  }

  private void addSupportedCollections(Class doc) {
    Collection persons = new Collection(Vocab.personCollection, Core.person);
    Collection events = new Collection(Vocab.eventCollection, Core.event);
    doc.add(Hydra.supportedClass, events, persons);
  }

}
