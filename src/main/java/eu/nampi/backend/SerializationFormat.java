package eu.nampi.backend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SerializationFormat {

    JSON_LD("JSON-LD"), N_TRIPLE("N-TRIPLE"), RDF_XML("RDF/XML"), TURTLE("TURTLE");

    private final String value;

}