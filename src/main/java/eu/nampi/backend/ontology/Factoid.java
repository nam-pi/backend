package eu.nampi.backend.ontology;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Vocabulary definition for the <a href="http://purl.com/net/factoid"> Factoid
 * Model</a>
 */
class Factoid {

    /**
     * The RDF model that holds the Factoid entities
     */
    private static final Model MODEL = ModelFactory.createDefaultModel();

    /**
     * The namespace of the Factoid vocabulary as a string
     */
    public static final String URI = "http://purl.com/net/factoid#";

    /**
     * The namespace of the Factoid vocabulary
     */
    public static final Resource NAMESPACE = MODEL.createResource(URI);

    /*
     * Class declarations
     */
    public static final Resource Agent = MODEL.createResource(URI + "Agent");
    public static final Resource Document = MODEL.createResource(URI + "Document");
    public static final Resource DocumentInterpretationAct = MODEL.createResource(URI + "Document_Interpretation_Act");
    public static final Resource Event = MODEL.createResource(URI + "Event");
    public static final Resource Organization = MODEL.createResource(URI + "Organization");
    public static final Resource Person = MODEL.createResource(URI + "Person");
    public static final Resource Situation = MODEL.createResource(URI + "Situation");
    public static final Resource TemporalEntity = MODEL.createResource(URI + "Temporal_Entity");

    /*
     * Property declarations
     */
    public static final Property hasAuthor = MODEL.createProperty(URI + "has_author");
    public static final Property hasInterpretation = MODEL.createProperty(URI + "has_interpretation");
    public static final Property hadParticipant = MODEL.createProperty(URI + "had_participant");
    public static final Property hasSource = MODEL.createProperty(URI + "has_source");
    public static final Property tookPartIn = MODEL.createProperty(URI + "took_part_in");

    /**
     * Returns the URI of the Factoid vocabulary as a string
     * 
     * @return the URI of the Factoid vocabulary
     * @see #URI;
     */
    public static final String getURI() {
        return URI;
    }

}