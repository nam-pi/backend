package eu.nampi.backend.converter;

import java.util.Optional;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.core.convert.converter.Converter;
import eu.nampi.backend.model.ResourceCouple;

public class StringToResourceCouple implements Converter<String, ResourceCouple> {
  @Override
  public ResourceCouple convert(String string) {
    if (!string.contains("|")) {
      return new ResourceCouple(Optional.empty(), ResourceFactory.createResource(string));
    }
    String[] parts = string.split("\\|");
    if (parts.length != 2) {
      throw new IllegalArgumentException(
          String.format("'%s' is not a valid Property - Resource pair", string));
    }
    return new ResourceCouple(Optional.of(ResourceFactory.createProperty(parts[0])),
        ResourceFactory.createResource(parts[1]));
  }
}
