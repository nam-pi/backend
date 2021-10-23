package eu.nampi.backend.deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.nampi.backend.converter.StringToResourceCouple;
import eu.nampi.backend.model.ResourceCouple;

public class ResourceCoupleListDeserializer extends StdDeserializer<List<ResourceCouple>> {

  private static final long serialVersionUID = 1L;

  public ResourceCoupleListDeserializer() {
    super(String.class);
  }

  private static final TypeReference<List<String>> LIST_OF_STRINGS_TYPE =
      new TypeReference<List<String>>() {};

  @Override
  public List<ResourceCouple> deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    List<ResourceCouple> resources = new ArrayList<>();
    JsonToken token = p.currentToken();
    if (JsonToken.START_ARRAY.equals(token)) {
      List<String> tokens = p.readValueAs(LIST_OF_STRINGS_TYPE);
      resources.addAll(tokens
          .stream()
          .map(s -> new StringToResourceCouple().convert(s))
          .collect(Collectors.toList()));
    }
    return resources;
  }
}
