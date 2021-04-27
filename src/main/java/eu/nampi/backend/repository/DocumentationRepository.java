package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
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
        .setNsPrefix("schema", SchemaOrg.getURI()).setNsPrefix("xsd", XSD.getURI())
        .setNsPrefix("core", Core.getURI());
    Class doc = new Class(Doc.baseUri, "The NAMPI API documentation", Hydra.ApiDocumentation);
    doc.addDescription(
        "The documentation for NAMPI, an API for the prosopographical data of the project 'Nuns and Monks - Prosopograpical Interfaces'");
    addEntryPoint(doc);
    addActClass(doc);
    addAspectClass(doc);
    addAuthorClass(doc);
    addDateClass(doc);
    addEventClass(doc);
    addGroupClass(doc);
    addPersonClass(doc);
    addPlaceClass(doc);
    addSourceClass(doc);
    addSourceLocationClass(doc);
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

  private void addActClass(Class doc) {
    Class act = new Class(Core.act, Core.act.getLocalName());
    act.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    act.addSupportedProperty(new SupportedProperty(Core.hasInterpretation, Core.event,
        Core.hasInterpretation.getLocalName(), true, false, false));
    act.addSupportedProperty(new SupportedProperty(Core.hasSourceLocation, Core.sourceLocation,
        Core.hasSourceLocation.getLocalName(), true, false, false));
    act.addSupportedProperty(new SupportedProperty(Core.isAuthoredBy, Core.author,
        Core.isAuthoredBy.getLocalName(), true, false, false));
    act.addSupportedProperty(new SupportedProperty(Core.isAuthoredOn, Core.date,
        Core.isAuthoredOn.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, act);
  }

  private void addAspectClass(Class doc) {
    Class aspect = new Class(Core.aspect, Core.aspect.getLocalName());
    aspect.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    aspect.addSupportedProperty(new SupportedProperty(Core.hasXsdString, XSD.xstring,
        Core.hasXsdString.getLocalName(), true, false, false));
    aspect.addSupportedProperty(new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL,
        SchemaOrg.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, aspect);
  }

  private void addAuthorClass(Class doc) {
    Class author = new Class(Core.author, Core.author.getLocalName());
    author.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, author);
  }

  private void addDateClass(Class doc) {
    Class date = new Class(Core.date, Core.date.getLocalName());
    date.addSupportedProperty(new SupportedProperty(Core.hasXsdDateTime, XSD.dateTime,
        Core.hasXsdDateTime.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, date);
  }

  private void addEventClass(Class doc) {
    Class event = new Class(Core.event, Core.event.getLocalName());
    event.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.hasSortingDate, Core.date,
        Core.hasSortingDate.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.takesPlaceNotEarlierThan, Core.date,
        Core.takesPlaceNotEarlierThan.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.takesPlaceNotLaterThan, Core.date,
        Core.takesPlaceNotLaterThan.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.takesPlaceOn, Core.date,
        Core.takesPlaceOn.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.takesPlaceAt, Core.place,
        Core.takesPlaceAt.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.hasMainParticipant, Core.person,
        Core.hasMainParticipant.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.usesAspect, Core.aspect,
        Core.usesAspect.getLocalName(), true, true, true));
    doc.add(Hydra.supportedClass, event);
  }

  private void addGroupClass(Class doc) {
    Class group = new Class(Core.group, Core.group.getLocalName());
    group.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    group.addSupportedProperty(new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL,
        SchemaOrg.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, group);
  }

  private void addPersonClass(Class doc) {
    Class person = new Class(Core.person, Core.person.getLocalName());
    person.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    person.addSupportedProperty(new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL,
        SchemaOrg.sameAs.getLocalName(), true, false, false));
    person.addSupportedProperty(new SupportedProperty(Core.isBornIn, Core.event,
        Core.isBornIn.getLocalName(), true, false, false));
    person.addSupportedProperty(new SupportedProperty(Core.diesIn, Core.event,
        Core.diesIn.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, person);
  }

  private void addPlaceClass(Class doc) {
    Class place = new Class(Core.place, Core.place.getLocalName());
    place.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    place.addSupportedProperty(new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL,
        SchemaOrg.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, place);
  }

  private void addSourceClass(Class doc) {
    Class source = new Class(Core.source, Core.source.getLocalName());
    source.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    source.addSupportedProperty(new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL,
        SchemaOrg.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, source);
  }

  private void addSourceLocationClass(Class doc) {
    Class sourceLocation = new Class(Core.sourceLocation, Core.sourceLocation.getLocalName());
    sourceLocation.addSupportedProperty(new SupportedProperty(Core.hasSource, Core.source,
        Core.hasSource.getLocalName(), true, false, false));
    sourceLocation.addSupportedProperty(new SupportedProperty(Core.hasXsdString, XSD.xstring,
        Core.hasXsdString.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, sourceLocation);
  }

  private void addUserClass(Class doc) {
    Class user = new Class(Doc.user, Doc.user.getLocalName());
    user.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    user.addSupportedProperty(new SupportedProperty(SchemaOrg.familyName, XSD.xstring,
        SchemaOrg.familyName.getLocalName(), true, false, false));
    user.addSupportedProperty(new SupportedProperty(SchemaOrg.givenName, XSD.xstring,
        SchemaOrg.givenName.getLocalName(), true, false, false));
    user.addSupportedProperty(new SupportedProperty(SchemaOrg.name, XSD.xstring,
        SchemaOrg.name.getLocalName(), true, false, false));
    user.addSupportedProperty(new SupportedProperty(SchemaOrg.identifier, XSD.xstring,
        SchemaOrg.identifier.getNameSpace(), true, false, false));
    user.addSupportedProperty(new SupportedProperty(SchemaOrg.email, XSD.xstring,
        SchemaOrg.email.getLocalName(), true, false, false));
    user.addSupportedProperty(new SupportedProperty(SchemaOrg.sameAs, SchemaOrg.URL,
        SchemaOrg.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, user);
  }

  private void addSupportedCollections(Class doc) {
    Collection acts = new Collection(Doc.actCollection, Core.act);
    Collection aspects = new Collection(Doc.aspectCollection, Core.aspect);
    Collection authors = new Collection(Doc.authorCollection, Core.author);
    Collection events = new Collection(Doc.eventCollection, Core.event);
    Collection groups = new Collection(Doc.groupCollection, Core.group);
    Collection persons = new Collection(Doc.personCollection, Core.person);
    Collection places = new Collection(Doc.placeCollection, Core.place);
    Collection sources = new Collection(Doc.sourceCollection, Core.source);
    doc.add(Hydra.supportedClass, acts, aspects, authors, events, groups, persons, places, sources);
  }

}
