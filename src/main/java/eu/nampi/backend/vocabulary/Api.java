package eu.nampi.backend.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * The API documentation vocabulary.
 */
public class Api {

  /**
   * The namespace of the vocabulary as a string
   */
  public static String uri = "https://purl.org/nampi/owl/api#";

  /**
   * returns the URI for this schema
   * 
   * @return the URI for this schema
   */
  public static String getURI() {
    return uri;
  }

  protected static Resource resource(String local) {
    return ResourceFactory.createResource(uri + local);
  }

  protected static Property property(String local) {
    return ResourceFactory.createProperty(uri, local);
  }

  public static Property classOrderByVar = property("classOrderByVariable");

  public static Property classCollection = property("classCollection");

  public static Property eventOrderByVar = property("eventOrderByVariable");

  public static Property eventDatesVar = property("eventDatesVariable");

  public static Property eventAspectVar = property("eventAspectVariable");

  public static Property eventAspectTypeVar = property("eventAspectTypeVariable");

  public static Property eventAspectUseTypeVar = property("eventAspectUseTypeVariable");

  public static Property eventPlaceVar = property("eventPlaceVariable");

  public static Property eventParticipantVar = property("eventParticipantVariable");

  public static Property eventParticipantTypeVar = property("eventParticipantTypeVariable");

  public static Property eventParticipationTypeVar = property("eventParticipationTypeVariable");

  public static Property personOrderByVar = property("personOrderByVariable");

  public static Property aspectCollection = property("aspectCollection");

  public static Property aspectOrderByVar = property("aspectOrderByVariable");

  public static Property aspectPersonVar = property("aspectPersonVariable");

  public static Property authorCollection = property("authorCollection");

  public static Property authorOrderByVar = property("authorOrderByVariable");

  public static Property textVar = property("textVariable");

  public static Property typeVar = property("typeVariable");

  public static Property events = property("events");

  public static Property persons = property("persons");

  public static Property entrypoint = property("entrypoint");

  public static Property eventCollection = property("eventCollection");

  public static Property actCollection = property("actCollection");

  public static Property actOrderByVar = property("actOrderByVariable");

  public static Property actAuthorVar = property("actAuthorVariable");

  public static Property actSourceVar = property("actSourceVariable");

  public static Property personCollection = property("personCollection");

  public static Property groupCollection = property("groupCollection");

  public static Property groupOrderByVar = property("groupOrderByVariable");

  public static Property placeCollection = property("placeCollection");

  public static Property placeOrderByVar = property("placeOrderByVariable");

  public static Property sourceCollection = property("sourceCollection");

  public static Property sourceOrderByVar = property("sourceOrderByVariable");

  public static Property user = property("user");
}
