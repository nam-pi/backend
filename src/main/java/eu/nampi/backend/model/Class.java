package eu.nampi.backend.model;

import org.apache.jena.rdf.model.Resource;
import eu.nampi.backend.vocabulary.Hydra;

public class Class extends AbstractHydraNode {

  public Class(String idUrl, String title) {
    super(idUrl, title, Hydra.Class);
  }

  public Class(Resource idProperty, String title) {
    super(idProperty.getURI(), title, Hydra.Class);
  }

  public Class(String idUrl, String title, Resource type) {
    super(idUrl, title, type);
  }

  public Class addSupportedProperty(SupportedProperty property) {
    add(Hydra.supportedProperty, property.base());
    add(property);
    return this;
  }

  public Class addSupportedOperation(SupportedOperation operation) {
    add(Hydra.supportedOperation, operation.base());
    add(operation);
    return this;
  }
}
