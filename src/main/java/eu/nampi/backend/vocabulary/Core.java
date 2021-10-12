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

  public static final Property aspectIsUsedIn = property("aspect_is_used_in");
  public static final Property diesIn = property("dies_in");
  public static final Property endsLifeOf = property("ends_life_of");
  public static final Property hasDate = property("has_date");
  public static final Property hasDateTime = property("has_date_time");
  public static final Property hasInterpretation = property("has_interpretation");
  public static final Property hasLatitude = property("has_latitude");
  public static final Property hasLongitude = property("has_longitude");
  public static final Property hasMainParticipant = property("has_main_participant");
  public static final Property hasPart = property("has_part");
  public static final Property hasParticipant = property("has_participant");
  public static final Property hasSortingDate = property("has_sorting_date");
  public static final Property hasSource = property("has_source");
  public static final Property hasSourceLocation = property("has_source_location");
  public static final Property hasText = property("has_text");
  public static final Property hasUrl = property("has_url");
  public static final Property hasValue = property("has_value");
  public static final Property isAuthoredBy = property("is_authored_by");
  public static final Property isAuthoredOn = property("is_authored_on");
  public static final Property isBornIn = property("is_born_in");
  public static final Property isInterpretationOf = property("is_interpretation_of");
  public static final Property isPartOf = property("is_part_of");
  public static final Property sameAs = property("same_as");
  public static final Property startsLifeOf = property("starts_life_of");
  public static final Property takesPlaceAt = property("takes_place_at");
  public static final Property takesPlaceNotEarlierThan = property("takes_place_not_earlier_than");
  public static final Property takesPlaceNotLaterThan = property("takes_place_not_later_than");
  public static final Property takesPlaceOn = property("takes_place_on");
  public static final Property usesAspect = property("uses_aspect");
  public static final Resource act = resource("act");
  public static final Resource actor = resource("actor");
  public static final Resource aspect = resource("aspect");
  public static final Resource author = resource("author");
  public static final Resource date = resource("date");
  public static final Resource event = resource("event");
  public static final Resource group = resource("group");
  public static final Resource person = resource("person");
  public static final Resource place = property("place");
  public static final Resource source = resource("source");
  public static final Resource sourceLocation = resource("source_location");
}
