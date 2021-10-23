package eu.nampi.backend.deserializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.jena.rdf.model.Literal;
import eu.nampi.backend.converter.StringToLiteralConverter;

public class LiteralDeserializer extends StdDeserializer<Literal> {

  private static final long serialVersionUID = 1L;

  public LiteralDeserializer() {
    super(String.class);
  }

  @Override
  public Literal deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    return new StringToLiteralConverter().convert(p.getText());
  }
}
