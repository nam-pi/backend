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

  public static final Property eventOrderByVar = property("event_order_by_variable");

  public static final Property eventDatesVar = property("event_dates_variable");

  public static final Property eventStatusTypeVar = property("event_status_type_variable");

  public static final Property eventOccupationTypeVar = property("event_occupation_type_variable");

  public static final Property personOrderByVar = property("person_order_by_variable");

  public static final Property statusOrderByVar = property("status_order_by_variable");

  public static final Property textVar = property("text_variable");

  public static final Property typeVar = property("type_variable");

}
