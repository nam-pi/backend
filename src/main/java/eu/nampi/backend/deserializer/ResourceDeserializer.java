package eu.nampi.backend.deserializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.jena.rdf.model.Resource;
import eu.nampi.backend.converter.StringToResourceConverter;

public class ResourceDeserializer extends StdDeserializer<Resource> {

  private static final long serialVersionUID = 1L;

  public ResourceDeserializer() {
    super(String.class);
  }

  @Override
  public Resource deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    return new StringToResourceConverter().convert(p.getText());
  }
}
