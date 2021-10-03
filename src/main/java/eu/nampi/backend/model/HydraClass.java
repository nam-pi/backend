package eu.nampi.backend.model;

import org.apache.jena.rdf.model.Resource;
import eu.nampi.backend.vocabulary.Hydra;

public class HydraClass extends AbstractHydraNode {

  public HydraClass(String idUrl, String title) {
    super(idUrl, title, Hydra.Class);
  }

  public HydraClass(Resource idProperty, String title) {
    super(idProperty.getURI(), title, Hydra.Class);
  }

  public HydraClass(String idUrl, String title, Resource type) {
    super(idUrl, title, type);
  }

  public HydraClass addSupportedProperty(HydraSupportedProperty property) {
    add(Hydra.supportedProperty, property.base());
    add(property);
    return this;
  }

  public HydraClass addSupportedOperation(HydraSupportedOperation operation) {
    add(Hydra.supportedOperation, operation.base());
    add(operation);
    return this;
  }
}
