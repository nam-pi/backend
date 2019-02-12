package eu.nampi.backend;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.jena.rdf.model.Model;

import eu.nampi.backend.exception.SerializationException;

public class ModelSerializer {

    public static String serialize(Model model, SerializationFormat format) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        model.write(stream, format.getValue());
        try {
            return stream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new SerializationException();
        }
    }

    public static String forContentType(Model model, String contentType) {
        switch (contentType) {
        case "application/n-triples":
            return ModelSerializer.toNTriples(model);
        case "application/rdf+xml":
            return ModelSerializer.toRdfXml(model);
        case "text/turtle":
            return ModelSerializer.toTurtle(model);
        default:
            return ModelSerializer.toJsonLd(model);
        }
    }

    public static String toJsonLd(Model model) {
        return serialize(model, SerializationFormat.JSON_LD);
    }

    public static String toNTriples(Model model) {
        return serialize(model, SerializationFormat.N_TRIPLE);
    }

    public static String toRdfXml(Model model) {
        return serialize(model, SerializationFormat.RDF_XML);
    }

    public static String toTurtle(Model model) {
        return serialize(model, SerializationFormat.TURTLE);
    }

}