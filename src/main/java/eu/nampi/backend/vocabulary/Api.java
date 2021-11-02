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
  public static String uri = "http://purl.org/nampi/owl/api#";

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

  public static Property ancestorOf = property("ancestorOf");
  public static Property descendantOf = property("descendantOf");
  public static Property hierarchyRoot = property("hierarchyRoot");
  public static Property isAuthor = property("isAuthor");

  public static Resource actAuthorProp = resource("actAuthorProperty");
  public static Resource actCollection = resource("actCollection");
  public static Resource actOrderByProp = resource("actOrderByProperty");
  public static Resource actSourceProp = resource("actSourceProperty");
  public static Resource aspectCollection = resource("aspectCollection");
  public static Resource aspectOrderByProp = resource("aspectOrderByProperty");
  public static Resource aspectParticipantProp = resource("aspectParticipantProperty");
  public static Resource authorCollection = resource("authorCollection");
  public static Resource authorOrderByProp = resource("authorOrderByProperty");
  public static Resource entrypoint = resource("entrypoint");
  public static Resource eventAspectTypeProp = resource("eventAspectTypeProperty");
  public static Resource eventAspectUseTypeProp = resource("eventAspectUseTypeProperty");
  public static Resource eventAspectProp = resource("eventAspectProperty");
  public static Resource eventAuthorProp = resource("eventAuthorProperty");
  public static Resource eventCollection = resource("eventCollection");
  public static Resource eventDatesProp = resource("eventDatesProperty");
  public static Resource eventOrderByProp = resource("eventOrderByProperty");
  public static Resource eventParticipantTypeProp = resource("eventParticipantTypeProperty");
  public static Resource eventParticipantProp = resource("eventParticipantProperty");
  public static Resource eventParticipationTypeProp = resource("eventParticipationTypeProperty");
  public static Resource eventPlaceProp = resource("eventPlaceProperty");
  public static Resource eventSourceProp = resource("eventSourceProperty");
  public static Resource groupCollection = resource("groupCollection");
  public static Resource groupHasPartProp = resource("groupHasPartProperty");
  public static Resource groupOrderByProp = resource("groupOrderByProperty");
  public static Resource groupPartOfProp = resource("groupPartOfProperty");
  public static Resource hierarchy = property("hierarchy");
  public static Resource personAspectProp = resource("personAspectProperty");
  public static Resource personCollection = resource("personCollection");
  public static Resource personOrderByProp = resource("personOrderByProperty");
  public static Resource placeCollection = resource("placeCollection");
  public static Resource placeOrderByProp = resource("placeOrderByProperty");
  public static Resource sourceCollection = resource("sourceCollection");
  public static Resource sourceOrderByProp = resource("sourceOrderByProperty");
  public static Resource textProp = resource("textProperty");
  public static Resource typeCollection = resource("typeCollection");
  public static Resource typeOrderByProp = resource("typeOrderByProperty");
  public static Resource user = resource("user");
}
