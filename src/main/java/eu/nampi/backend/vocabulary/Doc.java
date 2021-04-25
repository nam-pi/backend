package eu.nampi.backend.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * The API documentation vocabulary.
 */
public class Doc {

  public static final String baseUri =
      ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/doc";

  /**
   * The namespace of the vocabulary as a string
   */
  public static final String uri = baseUri + "#";

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

  public static final Property eventOrderByVar = property("eventOrderByVariable");

  public static final Property eventDatesVar = property("eventDatesVariable");

  public static final Property eventAspectVar = property("eventAspectVariable");

  public static final Property eventAspectTypeVar = property("eventAspectTypeVariable");

  public static final Property eventAspectUseTypeVar = property("eventAspectUseTypeVariable");

  public static final Property eventParticipantVar = property("eventParticipantVariable");

  public static final Property eventParticipantTypeVar = property("eventParticipantTypeVariable");

  public static final Property eventParticipationTypeVar =
      property("eventParticipationTypeVariable");

  public static final Property personOrderByVar = property("personOrderByVariable");

  public static final Property aspectCollection = property("aspectCollection");

  public static final Property aspectOrderByVar = property("aspectOrderByVariable");

  public static final Property aspectPersonVar = property("aspectPersonVariable");

  public static final Property textVar = property("textVariable");

  public static final Property typeVar = property("typeVariable");

  public static final Property events = property("events");

  public static final Property persons = property("persons");

  public static final Property entrypoint = property("entrypoint");

  public static final Property eventCollection = property("eventCollection");

  public static final Property personCollection = property("personCollection");

  public static final Property user = property("user");
}
