package eu.nampi.backend.ontology;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Vocabulary definition for the <a href="http://purl.org/nampi/ontologies/core"> Nampi Core
 * Model</a>
 */
public class NampiCore {

  public static final String BASE_URI = "http://purl.org/nampi/";

  /**
   * The RDF model that holds the Nampi Core entities
   */
  private static final Model MODEL = ModelFactory.createDefaultModel();

  /**
   * The namespace of the Nampi Core vocabulary as a string
   */
  public static final String URI = NampiCore.BASE_URI + "ontologies/core/";

  /**
   * The namespace of the Nampi Core vocabulary
   */
  public static final Resource NAMESPACE = MODEL.createResource(URI);

  /*
   * Class declarations
   */
  public static final Resource User = MODEL.createResource(URI + "User");

  /*
   * Property declarations
   */
  public static final Property email = MODEL.createProperty(URI + "email");
  public static final Property username = MODEL.createProperty(URI + "username");

  /**
   * Returns the URI of the Nampi Core vocabulary as a string
   *
   * @return the URI of the Nampi Core vocabulary
   * @see #URI;
   */
  public static final String getURI() {
    return URI;
  }

}
