package eu.nampi.backend.model.hydra;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.vocabulary.RDF;
import eu.nampi.backend.vocabulary.Hydra;

public abstract class AbstractHydraNode extends ModelCom {

  protected Resource base;

  private AbstractHydraNode(String title, Resource type, Resource base) {
    super(ModelFactory.createDefaultModel().getGraph());
    this.base = base;
    this.add(base, RDF.type, type);
    this.add(base, Hydra.title, ResourceFactory.createLangLiteral(title, "en"));
  }

  public AbstractHydraNode(String title, Resource type) {
    this(title, type, ResourceFactory.createResource());
  }

  public AbstractHydraNode(String idUrl, String title, Resource type) {
    this(title, type, ResourceFactory.createResource(idUrl));
  }

  public AbstractHydraNode addDescription(String description) {
    this.add(this.base, Hydra.description, ResourceFactory.createLangLiteral(description, "en"));
    return this;
  }

  public Resource base() {
    return base;
  }

  public void add(Property predicate, RDFNode object) {
    this.add(this.base, predicate, object);
  }

  public void add(Property predicate, String object) {
    this.add(this.base, predicate, object);
  }

  public void add(Property predicate, Class object) {
    this.add(this.base, predicate, object.base());
    this.add(object);
  }

  public void add(Property predicate, Class... objects) {
    for (Class o : objects) {
      this.add(predicate, o);
    }
  }
}
