package eu.nampi.backend.converter;

import org.apache.jena.riot.Lang;
import org.springframework.core.convert.converter.Converter;

public class StringToLangConverter implements Converter<String, Lang> {

  @Override
  public Lang convert(String source) {
    switch (source.toLowerCase()) {
    case "application/ld+json":
      return Lang.JSONLD;
    case "text/turtle":
      return Lang.TURTLE;
    case "application/rdf+xml":
      return Lang.RDFXML;
    case "application/n-triples":
      return Lang.NTRIPLES;
    default:
      return Lang.JSONLD;
    }
  }
}