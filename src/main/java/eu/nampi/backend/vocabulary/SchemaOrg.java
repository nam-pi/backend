package eu.nampi.backend.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SchemaOrg {

  /**
   * The namespace of the vocabulary as a string
   */
  public static final String uri = "https://schema.org/";

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

  public static final Property EntryPoint = property("EntryPoint");

  public static final Property familyName = property("familyName");

  public static final Property givenName = property("givenName");

  public static final Property name = property("name");

  public static final Property identifier = property("identifier");

  public static final Property email = property("email");

  public static final Property sameAs = property("sameAs");
}
