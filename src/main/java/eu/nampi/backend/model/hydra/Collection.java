package eu.nampi.backend.model.hydra;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFS;

import eu.nampi.backend.vocabulary.Hydra;

public class Collection extends Class {

  public Collection(Property type, Property itemType) {
    super(type.getURI(), type.getLocalName());
    add(RDFS.subClassOf, Hydra.Collection);
    SupportedProperty manages = new SupportedProperty(Hydra.manages, true, false, false);
    manages.addDescription("The content the collection manages");
    addSupportedProperty(manages);
    SupportedProperty member = new SupportedProperty(Hydra.member, true, false, false);
    member.addDescription("The members of the collection");
    addSupportedProperty(member);
    SupportedProperty search = new SupportedProperty(Hydra.search, true, false, true);
    search.addDescription("The available search parameters for this collection");
    addSupportedProperty(search);
    SupportedProperty totalItems = new SupportedProperty(Hydra.totalItems, true, false, false);
    totalItems.addDescription("The total number of items in this collection");
    addSupportedProperty(totalItems);
    SupportedProperty view = new SupportedProperty(Hydra.view, true, false, false);
    view.addDescription("The available views for this collection");
    addSupportedProperty(view);
  }

}
