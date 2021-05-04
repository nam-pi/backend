package eu.nampi.backend.model.hydra;

import org.apache.jena.rdf.model.Property;

import eu.nampi.backend.vocabulary.Hydra;

public class Class extends AbstractHydraNode {

  public Class(String idUrl, String title) {
    super(idUrl, title, Hydra.Class);
  }

  public Class(Property idProperty, String title) {
    super(idProperty.getURI(), title, Hydra.Class);
  }

  public Class(String idUrl, String title, Property type) {
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
