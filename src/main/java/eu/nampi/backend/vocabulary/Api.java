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

  @Deprecated
  public static Resource classCollection = resource("classCollection");
  @Deprecated
  public static Resource classOrderByVar = resource("classOrderByVariable");

  public static Property descendantOf = property("descendantOf");
  public static Property hierarchyRoot = property("hierarchyRoot");
  public static Property isAuthor = property("is_author");
  public static Resource actAuthorVar = resource("actAuthorVariable");
  public static Resource actCollection = resource("actCollection");
  public static Resource actOrderByVar = resource("actOrderByVariable");
  public static Resource actSourceVar = resource("actSourceVariable");
  public static Resource aspectCollection = resource("aspectCollection");
  public static Resource aspectOrderByVar = resource("aspectOrderByVariable");
  public static Resource aspectParticipantVar = resource("aspectParticipantVariable");
  public static Resource authorCollection = resource("authorCollection");
  public static Resource authorOrderByVar = resource("authorOrderByVariable");
  public static Resource entrypoint = resource("entrypoint");
  public static Resource eventAspectTypeVar = resource("eventAspectTypeVariable");
  public static Resource eventAspectUseTypeVar = resource("eventAspectUseTypeVariable");
  public static Resource eventAspectVar = resource("eventAspectVariable");
  public static Resource eventAuthorVar = resource("eventAuthorVariable");
  public static Resource eventCollection = resource("eventCollection");
  public static Resource eventDatesVar = resource("eventDatesVariable");
  public static Resource eventOrderByVar = resource("eventOrderByVariable");
  public static Resource eventParticipantTypeVar = resource("eventParticipantTypeVariable");
  public static Resource eventParticipantVar = resource("eventParticipantVariable");
  public static Resource eventParticipationTypeVar = resource("eventParticipationTypeVariable");
  public static Resource eventPlaceVar = resource("eventPlaceVariable");
  public static Resource events = resource("events");
  public static Resource groupCollection = resource("groupCollection");
  public static Resource groupHasPartVar = resource("groupHasPartVariable");
  public static Resource groupOrderByVar = resource("groupOrderByVariable");
  public static Resource groupPartOfVar = resource("groupPartOfVariable");
  public static Resource hierarchy = property("hierarchy");
  public static Resource personAspectVar = resource("personAspectVariable");
  public static Resource personCollection = resource("personCollection");
  public static Resource personOrderByVar = resource("personOrderByVariable");
  public static Resource persons = resource("persons");
  public static Resource placeCollection = resource("placeCollection");
  public static Resource placeOrderByVar = resource("placeOrderByVariable");
  public static Resource sourceCollection = resource("sourceCollection");
  public static Resource sourceOrderByVar = resource("sourceOrderByVariable");
  public static Resource textVar = resource("textVariable");
  public static Resource typeCollection = resource("typeCollection");
  public static Resource typeOrderByVar = resource("typeOrderByVariable");
  public static Resource typeVar = resource("typeVariable");
  public static Resource user = resource("user");
}
