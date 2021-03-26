package eu.nampi.backend.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * The NAMPI Core RDF vocabulary.
 */
public class Api {

  /**
   * The namespace of the vocabulary as a string
   */
  public static final String uri = "https://purl.org/nampi/owl/api#";

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

  public static final Property orderBy = property("orderBy");

  public static final Property type = property("type");

}
