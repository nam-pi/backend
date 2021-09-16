package eu.nampi.backend.repository;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.HydraClass;
import eu.nampi.backend.model.HydraCollection;
import eu.nampi.backend.model.HydraSupportedOperation;
import eu.nampi.backend.model.HydraSupportedProperty;
import eu.nampi.backend.util.Serializer;
import eu.nampi.backend.util.UrlBuilder;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.SchemaOrg;

@Repository
public class DocumentationRepository {

  @Autowired
  Serializer serializer;

  @Autowired
  UrlBuilder urlBuilder;

  public String get(Lang lang) {

    Model model = ModelFactory.createDefaultModel();
    model
        .setNsPrefix("api", Api.getURI())
        .setNsPrefix("hydra", Hydra.getURI())
        .setNsPrefix("rdf", RDF.getURI())
        .setNsPrefix("rdfs", RDFS.getURI())
        .setNsPrefix("schema", SchemaOrg.getURI())
        .setNsPrefix("xsd", XSD.getURI())
        .setNsPrefix("core", Core.getURI());
    HydraClass doc =
        new HydraClass(urlBuilder.endpointUri("doc"), "The NAMPI API documentation",
            Hydra.ApiDocumentation);
    doc.addDescription(
        "The documentation for NAMPI, an API for the prosopographical data of the project 'Nuns and Monks - Prosopograpical Interfaces'");
    addEntryPoint(doc);
    addActClass(doc);
    addAspectClass(doc);
    addAuthorClass(doc);
    addTypeClass(doc);
    addDateClass(doc);
    addEventClass(doc);
    addGroupClass(doc);
    addPersonClass(doc);
    addPlaceClass(doc);
    addSourceClass(doc);
    addSourceLocationClass(doc);
    addUserClass(doc);
    addSupportedCollections(doc);
    model
        .add(doc);
    return serializer.serialize(model, lang, doc.base());
  }

  private void addEntryPoint(HydraClass doc) {
    doc
        .add(Hydra.entrypoint, ResourceFactory.createProperty(urlBuilder.endpointUri()));
    HydraClass entrypoint = new HydraClass(Api.entrypoint, Api.entrypoint.getLocalName());
    entrypoint
        .addSupportedOperation(
            new HydraSupportedOperation("Gets the API entrypoint", HttpMethod.GET));
    doc
        .add(Hydra.supportedClass, entrypoint);
  }

  private void addActClass(HydraClass doc) {
    HydraClass act = new HydraClass(Core.act, Core.act.getLocalName());
    act.addSupportedProperty(
        new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(), true, false,
            false));
    act.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    act.addSupportedProperty(new HydraSupportedProperty(Core.hasInterpretation, Core.event,
        Core.hasInterpretation.getLocalName(), true, false, false));
    act.addSupportedProperty(new HydraSupportedProperty(Core.hasSourceLocation, Core.sourceLocation,
        Core.hasSourceLocation.getLocalName(), true, false, false));
    act.addSupportedProperty(new HydraSupportedProperty(Core.isAuthoredBy, Core.author,
        Core.isAuthoredBy.getLocalName(), true, false, false));
    act.addSupportedProperty(new HydraSupportedProperty(Core.isAuthoredOn, Core.date,
        Core.isAuthoredOn.getLocalName(), true, false, false));
    act.addSupportedProperty(new HydraSupportedProperty(RDFS.comment, RDF.langString,
        RDFS.comment.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, act);
  }

  private void addAspectClass(HydraClass doc) {
    HydraClass aspect = new HydraClass(Core.aspect, Core.aspect.getLocalName());
    aspect
        .addSupportedProperty(
            new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(),
                true, false, false));
    aspect.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    aspect.addSupportedProperty(new HydraSupportedProperty(RDFS.comment, RDF.langString,
        RDFS.comment.getLocalName(), true, false, false));
    aspect.addSupportedProperty(new HydraSupportedProperty(Core.hasText, RDF.langString,
        Core.hasText.getLocalName(), true, false, false));
    aspect.addSupportedProperty(new HydraSupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, aspect);
  }

  private void addAuthorClass(HydraClass doc) {
    HydraClass author = new HydraClass(Core.author, Core.author.getLocalName());
    author.addSupportedProperty(
        new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(), true, false,
            false));
    author.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    author.addSupportedProperty(new HydraSupportedProperty(RDFS.comment, RDF.langString,
        RDFS.comment.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, author);
  }

  private void addTypeClass(HydraClass doc) {
    HydraClass clss = new HydraClass(RDFS.Resource, RDFS.Resource.getLocalName());
    clss.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    clss.addSupportedProperty(new HydraSupportedProperty(RDFS.comment, RDF.langString,
        RDFS.comment.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, clss);
  }

  private void addDateClass(HydraClass doc) {
    HydraClass date = new HydraClass(Core.date, Core.date.getLocalName());
    date.addSupportedProperty(
        new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(), true, false,
            false));
    date.addSupportedProperty(new HydraSupportedProperty(Core.hasDateTime, XSD.dateTime,
        Core.hasDateTime.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, date);
  }

  private void addEventClass(HydraClass doc) {
    HydraClass event = new HydraClass(Core.event, Core.event.getLocalName());
    event.addSupportedProperty(
        new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(), true, false,
            false));
    event.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    event.addSupportedProperty(new HydraSupportedProperty(RDFS.comment, RDF.langString,
        RDFS.comment.getLocalName(), true, false, false));
    event.addSupportedProperty(new HydraSupportedProperty(Core.hasSortingDate, Core.date,
        Core.hasSortingDate.getLocalName(), true, false, false));
    event.addSupportedProperty(new HydraSupportedProperty(Core.takesPlaceNotEarlierThan, Core.date,
        Core.takesPlaceNotEarlierThan.getLocalName(), true, false, false));
    event.addSupportedProperty(new HydraSupportedProperty(Core.takesPlaceNotLaterThan, Core.date,
        Core.takesPlaceNotLaterThan.getLocalName(), true, false, false));
    event.addSupportedProperty(new HydraSupportedProperty(Core.takesPlaceOn, Core.date,
        Core.takesPlaceOn.getLocalName(), true, false, false));
    event.addSupportedProperty(new HydraSupportedProperty(Core.takesPlaceAt, Core.place,
        Core.takesPlaceAt.getLocalName(), true, false, false));
    event.addSupportedProperty(new HydraSupportedProperty(Core.hasMainParticipant, Core.person,
        Core.hasParticipant.getLocalName(), true, true, false));
    event.addSupportedProperty(new HydraSupportedProperty(Core.hasParticipant, Core.agent,
        Core.hasParticipant.getLocalName(), true, true, false));
    event.addSupportedProperty(new HydraSupportedProperty(Core.usesAspect, Core.aspect,
        Core.usesAspect.getLocalName(), true, true, false));
    event.addSupportedProperty(new HydraSupportedProperty(Core.isInterpretationOf, Core.act,
        Core.isInterpretationOf.getLocalName(), true, true, false));
    doc.add(Hydra.supportedClass, event);
  }

  private void addGroupClass(HydraClass doc) {
    HydraClass group = new HydraClass(Core.group, Core.group.getLocalName());
    group.addSupportedProperty(
        new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(), true, false,
            false));
    group.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    group.addSupportedProperty(new HydraSupportedProperty(RDFS.comment, RDF.langString,
        RDFS.comment.getLocalName(), true, false, false));
    group.addSupportedProperty(new HydraSupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, group);
  }

  private void addPersonClass(HydraClass doc) {
    HydraClass person = new HydraClass(Core.person, Core.person.getLocalName());
    person.addSupportedProperty(
        new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(), true, false,
            false));
    person.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    person.addSupportedProperty(new HydraSupportedProperty(RDFS.comment, RDF.langString,
        RDFS.comment.getLocalName(), true, false, false));
    person.addSupportedProperty(new HydraSupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    person.addSupportedProperty(new HydraSupportedProperty(Core.isBornIn, Core.event,
        Core.isBornIn.getLocalName(), true, false, false));
    person.addSupportedProperty(new HydraSupportedProperty(Core.diesIn, Core.event,
        Core.diesIn.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, person);
  }

  private void addPlaceClass(HydraClass doc) {
    HydraClass place = new HydraClass(Core.place, Core.place.getLocalName());
    place.addSupportedProperty(
        new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(), true, false,
            false));
    place.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    place.addSupportedProperty(new HydraSupportedProperty(RDFS.comment, RDF.langString,
        RDFS.comment.getLocalName(), true, false, false));
    place.addSupportedProperty(new HydraSupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, place);
  }

  private void addSourceClass(HydraClass doc) {
    HydraClass source = new HydraClass(Core.source, Core.source.getLocalName());
    source.addSupportedProperty(
        new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(), true, false,
            false));
    source.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    source.addSupportedProperty(new HydraSupportedProperty(RDFS.comment, RDF.langString,
        RDFS.comment.getLocalName(), true, false, false));
    source.addSupportedProperty(new HydraSupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, source);
  }

  private void addSourceLocationClass(HydraClass doc) {
    HydraClass sourceLocation =
        new HydraClass(Core.sourceLocation, Core.sourceLocation.getLocalName());
    sourceLocation.addSupportedProperty(
        new HydraSupportedProperty(RDF.type, RDFS.Class, RDF.type.getLocalName(), true, false,
            false));
    sourceLocation.addSupportedProperty(new HydraSupportedProperty(Core.hasSource, Core.source,
        Core.hasSource.getLocalName(), true, false, false));
    sourceLocation.addSupportedProperty(new HydraSupportedProperty(Core.hasText, XSD.xstring,
        Core.hasText.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, sourceLocation);
  }

  private void addUserClass(HydraClass doc) {
    HydraClass user = new HydraClass(Api.user, Api.user.getLocalName());
    user.addSupportedProperty(new HydraSupportedProperty(RDFS.label, RDF.langString,
        RDFS.label.getLocalName(), true, false, false));
    user.addSupportedProperty(new HydraSupportedProperty(SchemaOrg.familyName, XSD.xstring,
        SchemaOrg.familyName.getLocalName(), true, false, false));
    user.addSupportedProperty(new HydraSupportedProperty(SchemaOrg.givenName, XSD.xstring,
        SchemaOrg.givenName.getLocalName(), true, false, false));
    user.addSupportedProperty(new HydraSupportedProperty(SchemaOrg.name, XSD.xstring,
        SchemaOrg.name.getLocalName(), true, false, false));
    user.addSupportedProperty(new HydraSupportedProperty(SchemaOrg.identifier, XSD.xstring,
        SchemaOrg.identifier.getNameSpace(), true, false, false));
    user.addSupportedProperty(new HydraSupportedProperty(SchemaOrg.email, XSD.xstring,
        SchemaOrg.email.getLocalName(), true, false, false));
    user.addSupportedProperty(new HydraSupportedProperty(Core.sameAs, SchemaOrg.URL,
        Core.sameAs.getLocalName(), true, false, false));
    doc.add(Hydra.supportedClass, user);
  }

  private void addSupportedCollections(HydraClass doc) {
    HydraCollection acts = new HydraCollection(Api.actCollection, Core.act);
    HydraCollection aspects = new HydraCollection(Api.aspectCollection, Core.aspect);
    HydraCollection authors = new HydraCollection(Api.authorCollection, Core.author);
    HydraCollection events = new HydraCollection(Api.eventCollection, Core.event);
    HydraCollection groups = new HydraCollection(Api.groupCollection, Core.group);
    HydraCollection persons = new HydraCollection(Api.personCollection, Core.person);
    HydraCollection places = new HydraCollection(Api.placeCollection, Core.place);
    HydraCollection sources = new HydraCollection(Api.sourceCollection, Core.source);
    HydraCollection types = new HydraCollection(Api.typeCollection, RDFS.Resource);
    doc.add(Hydra.supportedClass, acts, aspects, authors, events, groups, persons, places, sources,
        types);
  }
}
