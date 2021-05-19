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
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.SchemaOrg;

@Repository
public class DocumentationRepository extends AbstractHydraRepository {

  public String get(Lang lang) {

    Model model = ModelFactory.createDefaultModel();
    model.setNsPrefix("api", Api.getURI()).setNsPrefix("hydra", Hydra.getURI())
        .setNsPrefix("rdf", RDF.getURI()).setNsPrefix("rdfs", RDFS.getURI())
        .setNsPrefix("schema", SchemaOrg.getURI()).setNsPrefix("xsd", XSD.getURI())
        .setNsPrefix("core", Core.getURI());
    Class doc =
        new Class(endpointUri("doc"), "The NAMPI API documentation", Hydra.ApiDocumentation);
    doc.addDescription(
        "The documentation for NAMPI, an API for the prosopographical data of the project 'Nuns and Monks - Prosopograpical Interfaces'");
    addEntryPoint(doc);
    addActClass(doc);
    addAspectClass(doc);
    addAuthorClass(doc);
    addClassClass(doc);
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
    Class entrypoint = new Class(Api.entrypoint, Api.entrypoint.getLocalName());
    entrypoint
        .addSupportedOperation(new SupportedOperation("Gets the API entrypoint", HttpMethod.GET));
    doc.add(Hydra.supportedClass, entrypoint);
  }

  private void addActClass(Class doc) {
    Class act = new Class(Core.act, Core.act.getLocalName());
    act.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
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
    act.addSupportedProperty(new SupportedProperty(RDFS.comment, XSD.xstring,
        RDFS.comment.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, act);
  }

  private void addAspectClass(Class doc) {
    Class aspect = new Class(Core.aspect, Core.aspect.getLocalName());
    aspect.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
    aspect.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    aspect.addSupportedProperty(new SupportedProperty(Core.hasText, XSD.xstring,
        Core.hasText.getLocalName(), true, false, false));
    aspect.addSupportedProperty(new SupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, aspect);
  }

  private void addAuthorClass(Class doc) {
    Class author = new Class(Core.author, Core.author.getLocalName());
    author.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
    author.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, author);
  }

  private void addClassClass(Class doc) {
    Class clss = new Class(RDFS.Class, RDFS.Class.getLocalName());
    clss.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    clss.addSupportedProperty(new SupportedProperty(RDFS.comment, XSD.xstring,
        RDFS.comment.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, clss);
  }

  private void addDateClass(Class doc) {
    Class date = new Class(Core.date, Core.date.getLocalName());
    date.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
    date.addSupportedProperty(new SupportedProperty(Core.hasDateTime, XSD.dateTime,
        Core.hasDateTime.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, date);
  }

  private void addEventClass(Class doc) {
    Class event = new Class(Core.event, Core.event.getLocalName());
    event.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
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
    event.addSupportedProperty(new SupportedProperty(Core.hasParticipant, Core.agent,
        Core.hasParticipant.getLocalName(), true, false, false));
    event.addSupportedProperty(new SupportedProperty(Core.usesAspect, Core.aspect,
        Core.usesAspect.getLocalName(), true, true, true));
    doc.add(Hydra.supportedClass, event);
  }

  private void addGroupClass(Class doc) {
    Class group = new Class(Core.group, Core.group.getLocalName());
    group.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
    group.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    group.addSupportedProperty(new SupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, group);
  }

  private void addPersonClass(Class doc) {
    Class person = new Class(Core.person, Core.person.getLocalName());
    person.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
    person.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    person.addSupportedProperty(new SupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    person.addSupportedProperty(new SupportedProperty(Core.isBornIn, Core.event,
        Core.isBornIn.getLocalName(), true, false, false));
    person.addSupportedProperty(new SupportedProperty(Core.diesIn, Core.event,
        Core.diesIn.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, person);
  }

  private void addPlaceClass(Class doc) {
    Class place = new Class(Core.place, Core.place.getLocalName());
    place.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
    place.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    place.addSupportedProperty(new SupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, place);
  }

  private void addSourceClass(Class doc) {
    Class source = new Class(Core.source, Core.source.getLocalName());
    source.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
    source.addSupportedProperty(new SupportedProperty(RDFS.label, XSD.xstring,
        RDFS.label.getLocalName(), true, false, false));
    source.addSupportedProperty(new SupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, source);
  }

  private void addSourceLocationClass(Class doc) {
    Class sourceLocation = new Class(Core.sourceLocation, Core.sourceLocation.getLocalName());
    sourceLocation.addSupportedProperty(new SupportedProperty(RDF.type, RDFS.Class,
        RDF.type.getLocalName(), true, false, false));
    sourceLocation.addSupportedProperty(new SupportedProperty(Core.hasSource, Core.source,
        Core.hasSource.getLocalName(), true, false, false));
    sourceLocation.addSupportedProperty(new SupportedProperty(Core.hasText, XSD.xstring,
        Core.hasText.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, sourceLocation);
  }

  private void addUserClass(Class doc) {
    Class user = new Class(Api.user, Api.user.getLocalName());
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
    user.addSupportedProperty(new SupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, user);
  }

  private void addSupportedCollections(Class doc) {
    Collection acts = new Collection(Api.actCollection, Core.act);
    Collection aspects = new Collection(Api.aspectCollection, Core.aspect);
    Collection authors = new Collection(Api.authorCollection, Core.author);
    Collection classes = new Collection(Api.classCollection, RDFS.Class);
    Collection events = new Collection(Api.eventCollection, Core.event);
    Collection groups = new Collection(Api.groupCollection, Core.group);
    Collection persons = new Collection(Api.personCollection, Core.person);
    Collection places = new Collection(Api.placeCollection, Core.place);
    Collection sources = new Collection(Api.sourceCollection, Core.source);
    doc.add(Hydra.supportedClass, acts, aspects, authors, classes, events, groups, persons, places,
        sources);
  }

}
