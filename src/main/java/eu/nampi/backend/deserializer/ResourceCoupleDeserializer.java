package eu.nampi.backend.deserializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.nampi.backend.converter.StringToResourceCouple;
import eu.nampi.backend.model.ResourceCouple;

public class ResourceCoupleDeserializer extends StdDeserializer<ResourceCouple> {

  private static final long serialVersionUID = 1L;

  public ResourceCoupleDeserializer() {
    super(String.class);
  }

  @Override
  public ResourceCouple deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    return new StringToResourceCouple().convert(p.getText());
  }
}
