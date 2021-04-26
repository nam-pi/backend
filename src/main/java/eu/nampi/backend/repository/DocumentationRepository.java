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
    addAspectClass(doc);
    addAuthorClass(doc);
    addEventClass(doc);
    addGroupClass(doc);
    addPersonClass(doc);
    addPlaceClass(doc);
    addSourceClass(doc);
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

  private void addAspectClass(Class doc) {
    Class aspect = new Class(Core.aspect, Core.aspect.getLocalName());
    aspect.addSupportedProperty(
        new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    aspect.addSupportedProperty(
        new SupportedProperty(Core.hasXsdString, "xsd:string", "label", true, false, false));
    aspect.addSupportedProperty(
        new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL, "sameAs", true, false, false));
    doc.add(Hydra.supportedClass, aspect);
  }

  private void addAuthorClass(Class doc) {
    Class author = new Class(Core.author, Core.author.getLocalName());
    author.addSupportedProperty(
        new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    doc.add(Hydra.supportedClass, author);
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

  private void addGroupClass(Class doc) {
    Class group = new Class(Core.group, Core.group.getLocalName());
    group.addSupportedProperty(
        new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    group.addSupportedProperty(
        new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL, "sameAs", true, false, false));
    doc.add(Hydra.supportedClass, group);
  }

  private void addPersonClass(Class doc) {
    Class person = new Class(Core.person, Core.person.getLocalName());
    person.addSupportedProperty(
        new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    person.addSupportedProperty(
        new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL, "sameAs", true, false, false));
    doc.add(Hydra.supportedClass, person);
  }

  private void addPlaceClass(Class doc) {
    Class place = new Class(Core.place, Core.place.getLocalName());
    place.addSupportedProperty(
        new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    place.addSupportedProperty(
        new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL, "sameAs", true, false, false));
    doc.add(Hydra.supportedClass, place);
  }

  private void addSourceClass(Class doc) {
    Class source = new Class(Core.source, Core.source.getLocalName());
    source.addSupportedProperty(
        new SupportedProperty(RDFS.label, "xsd:string", "label", true, false, false));
    source.addSupportedProperty(
        new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL, "sameAs", true, false, false));
    doc.add(Hydra.supportedClass, source);
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
        new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL, "sameAs", true, false, false));
    doc.add(Hydra.supportedClass, user);
  }

  private void addSupportedCollections(Class doc) {
    Collection aspects = new Collection(Doc.aspectCollection, Core.aspect);
    Collection authors = new Collection(Doc.authorCollection, Core.author);
    Collection events = new Collection(Doc.eventCollection, Core.event);
    Collection groups = new Collection(Doc.groupCollection, Core.group);
    Collection persons = new Collection(Doc.personCollection, Core.person);
    Collection places = new Collection(Doc.placeCollection, Core.place);
    Collection sources = new Collection(Doc.sourceCollection, Core.source);
    doc.add(Hydra.supportedClass, aspects, authors, events, groups, persons, places, sources);
  }

}
