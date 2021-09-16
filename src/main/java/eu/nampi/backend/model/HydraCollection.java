package eu.nampi.backend.model;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import eu.nampi.backend.vocabulary.Hydra;

public class HydraCollection extends HydraClass {

  public HydraCollection(Resource type, Resource itemType) {
    super(type.getURI(), type.getLocalName());
    add(RDFS.subClassOf, Hydra.Collection);

    HydraSupportedProperty manages = new HydraSupportedProperty(Hydra.manages, true, false, false);
    manages.addDescription("The content the collection manages");
    addSupportedProperty(manages);

    HydraSupportedProperty member = new HydraSupportedProperty(Hydra.member, true, false, false);
    member.addDescription("The members of the collection");
    addSupportedProperty(member);

    HydraSupportedProperty search = new HydraSupportedProperty(Hydra.search, true, false, true);
    search.addDescription("The available search parameters for this collection");
    addSupportedProperty(search);

    HydraSupportedProperty totalItems =
        new HydraSupportedProperty(Hydra.totalItems, true, false, false);
    totalItems.addDescription("The total number of items in this collection");
    addSupportedProperty(totalItems);

    HydraSupportedProperty view = new HydraSupportedProperty(Hydra.view, true, false, false);
    view.addDescription("The available views for this collection");
    addSupportedProperty(view);
  }
}
