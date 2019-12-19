package eu.nampi.backend.ontology;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.impl.OntModelImpl;

/**
 * Vocabulary definition for the <a href="http://purl.org/nampi/ontologies/core"> Nampi Core
 * Model</a>
 */
public class NampiCore extends OntModelImpl {

  public NampiCore() {
    super(OntModelSpec.OWL_MEM);
  }

}
