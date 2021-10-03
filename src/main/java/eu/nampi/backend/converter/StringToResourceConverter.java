package eu.nampi.backend.converter;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.core.convert.converter.Converter;

public class StringToResourceConverter implements Converter<String, Resource> {
  @Override
  public Resource convert(String string) {
    return ResourceFactory.createResource(string);
  }
}
