package eu.nampi.backend.converter;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.core.convert.converter.Converter;

public class StringToPropertyConverter implements Converter<String, Property> {
  @Override
  public Property convert(String string) {
    return ResourceFactory.createProperty(string);
  }
}
