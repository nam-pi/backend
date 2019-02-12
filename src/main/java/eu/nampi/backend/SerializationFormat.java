package eu.nampi.backend;

public enum SerializationFormat {

    JSON_LD("JSON-LD"), N_TRIPLE("N-TRIPLE"), RDF_XML("RDF/XML"), TURTLE("TURTLE");

    private final String value;

    SerializationFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}