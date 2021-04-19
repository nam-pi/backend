package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.hydra.Class;
import eu.nampi.backend.model.hydra.Collection;
import eu.nampi.backend.model.hydra.SupportedOperation;
import eu.nampi.backend.model.hydra.SupportedProperty;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.SchemaOrg;

@Repository
public class DocumentationRepository extends AbstractHydraRepository {

  public String get(Lang lang) {

    Model model = ModelFactory.createDefaultModel();
    model.setNsPrefix("doc", Doc.getURI()).setNsPrefix("hydra", Hydra.getURI())
        .setNsPrefix("rdf", RDF.getURI()).setNsPrefix("rdfs", RDFS.getURI())
        .setNsPrefix("schema", SchemaOrg.getURI());
    Class doc = new Class(Doc.baseUri, "The NAMPI API documentation", Hydra.ApiDocumentation);
    doc.addDescription(
        "The documentation for NAMPI, an API for the prosopographical data of the project 'Nuns and Monks - Prosopograpical Interfaces'");
    addEntryPoint(doc);
    addEventClass(doc);
    addPersonClass(doc);
    addUserClass(doc);
    addSupportedCollections(doc);
    model.add(doc);
    return serialize(model, lang, doc.base());
  }

  private void addEntryPoint(Class doc) {
    doc.add(Hydra.entrypoint, ResourceFactory.createProperty(endpointUri()));
    Class entrypoint = new Class(Doc.entrypoint, Doc.entrypoint.getLocalName());
    entrypoint
        .addSupportedOperation(new SupportedOperation("Gets the API entrypoint", HttpMethod.GET));
    doc.add(Hydra.supportedClass, entrypoint);
  }

  private void addEventClass(Class doc) {
    Class event = new Class(Core.event, Core.event.getLocalName());
    event.addSupportedProperty(
        new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.hasSortingDate, Core.date,
        Core.hasSortingDate.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.takesPlaceNotEarlierThan, Core.date,
        Core.takesPlaceNotEarlierThan.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.takesPlaceNotLaterThan, Core.date,
        Core.takesPlaceNotLaterThan.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.takesPlaceOn, Core.date,
        Core.takesPlaceOn.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, event);
  }

  private void addPersonClass(Class doc) {
    Class person = new Class(Core.person, Core.person.getLocalName());
    person.addSupportedProperty(
        new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    doc.add(Hydra.supportedClass, person);
  }

  private void addUserClass(Class doc) {
    Class user = new Class(Doc.user, Doc.user.getLocalName());
    user.addSupportedProperty(
        new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    user.addSupportedProperty(new SupportedProperty(SchemaOrg.familyName, "xsd:string",
        "familyName", true, false, false));
    user.addSupportedProperty(
        new SupportedProperty(SchemaOrg.givenName, "xsd:string", "givenName", true, false, false));
    user.addSupportedProperty(
        new SupportedProperty(SchemaOrg.name, "xsd:string", "username", true, false, false));
    user.addSupportedProperty(new SupportedProperty(SchemaOrg.identifier, "xsd:string",
        "identifier", true, false, false));
    user.addSupportedProperty(
        new SupportedProperty(SchemaOrg.email, "xsd:string", "email", true, false, false));
    user.addSupportedProperty(
        new SupportedProperty(SchemaOrg.sameAs, Core.author, "sameAs", true, false, false));
    doc.add(Hydra.supportedClass, user);
  }

  private void addSupportedCollections(Class doc) {
    Collection persons = new Collection(Doc.personCollection, Core.person);
    Collection events = new Collection(Doc.eventCollection, Core.event);
    doc.add(Hydra.supportedClass, events, persons);
  }

}
