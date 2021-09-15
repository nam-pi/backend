package eu.nampi.backend.model.hydra;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import eu.nampi.backend.vocabulary.Hydra;

public class SupportedProperty extends AbstractHydraNode {

  private Resource property;

  public SupportedProperty(Resource property, String title, boolean readable, boolean required,
      boolean writeable) {
    super(title, Hydra.SupportedProperty);
    this.property = property;
    add(Hydra.property, property);
    add(property, RDF.type, RDF.Property);
    add(Hydra.readable, readable ? "true" : "false");
    add(Hydra.required, required ? "true" : "false");
    add(Hydra.writeable, writeable ? "true" : "false");
  }

  public SupportedProperty(Resource property, boolean readable, boolean required,
      boolean writeable) {
    this(property, property.getLocalName(), readable, required, writeable);
  }

  public SupportedProperty(Resource property, String range, String title, boolean readable,
      boolean required, boolean writeable) {
    this(property, title, readable, required, writeable);
    add(property, RDFS.range, range);
  }

  public SupportedProperty(Resource property, Resource range, String title, boolean readable,
      boolean required, boolean writeable) {
    this(property, title, readable, required, writeable);
    add(property, RDFS.range, range);
  }

  public void addPropertyType(Resource type) {
    add(this.property, RDF.type, type);
  }

  public void makeLink() {
    addPropertyType(Hydra.Link);
  }
}
