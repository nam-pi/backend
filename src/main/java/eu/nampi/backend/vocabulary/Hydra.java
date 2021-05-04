package eu.nampi.backend.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * The Hydra Core RDF vocabulary.
 */
public class Hydra {

  /**
   * The namespace of the vocabulary as a string
   */
  public static final String uri = "http://www.w3.org/ns/hydra/core#";

  /**
   * returns the URI for this schema
   * 
   * @return the URI for this schema
   */
  public static String getURI() {
    return uri;
  }

  protected static final Resource resource(String local) {
    return ResourceFactory.createResource(uri + local);
  }

  protected static final Property property(String local) {
    return ResourceFactory.createProperty(uri, local);
  }

  public static final Property Resource = property("Resource");

  public static final Property collection = property("collection");

  public static final Property Collection = property("Collection");

  public static final Property member = property("member");

  public static final Property totalItems = property("totalItems");

  public static final Property first = property("first");

  public static final Property last = property("last");

  public static final Property next = property("next");

  public static final Property previous = property("previous");

  public static final Property PartialCollectionView = property("PartialCollectionView");

  public static final Property view = property("view");

  public static final Property search = property("search");

  public static final Property IriTemplate = property("IriTemplate");

  public static final Property template = property("template");

  public static final Property IriTemplateMapping = property("IriTemplateMapping");

  public static final Property mapping = property("mapping");

  public static final Property property = property("property");

  public static final Property readable = property("readable");

  public static final Property writeable = property("writeable");

  public static final Property required = property("required");

  public static final Property variable = property("variable");

  public static final Property pageIndex = property("pageIndex");

  public static final Property limit = property("limit");

  public static final Property offset = property("offset");

  public static final Property variableRepresentation = property("variableRepresentation");

  public static final Property BasicRepresentation = property("BasicRepresentation");

  public static final Property title = property("title");

  public static final Property description = property("description");

  public static final Property supportedClass = property("supportedClass");

  public static final Property Class = property("Class");

  public static final Property ApiDocumentation = property("ApiDocumentation");

  public static final Property entrypoint = property("entrypoint");

  public static final Property memberAssertion = property("memberAssertion");

  public static final Property manages = property("manages");

  public static final Property object = property("object");

  public static final Property supportedProperty = property("supportedProperty");

  public static final Property SupportedProperty = property("SupportedProperty");

  public static final Property Link = property("Link");

  public static final Property method = property("method");

  public static final Property supportedOperation = property("supportedOperation");

  public static final Property Operation = property("Operation");

}
