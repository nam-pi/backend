package eu.nampi.backend.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * The NAMPI Core RDF vocabulary.
 */
public class Core {

  /**
   * The namespace of the vocabulary as a string
   */
  public static final String uri = "https://purl.org/nampi/owl/core#";

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

  public static final Property date = property("date");

  public static final Property agent = property("agent");

  public static final Property event = property("event");

  public static final Property person = property("person");

  public static final Property group = property("group");

  public static final Property aspect = property("aspect");

  public static final Property usesAspect = property("uses_aspect");

  public static final Property hasParticipant = property("has_participant");

  public static final Property hasMainParticipant = property("has_main_participant");

  public static final Property hasSortingDate = property("has_sorting_date");

  public static final Property takesPlaceOn = property("takes_place_on");

  public static final Property takesPlaceNotEarlierThan = property("takes_place_not_earlier_than");

  public static final Property takesPlaceNotLaterThan = property("takes_place_not_later_than");

  public static final Property hasXsdDateTime = property("has_xsd_date_time");

  public static final Property hasXsdString = property("has_xsd_string");

  public static final Property author = property("author");
}
