package eu.nampi.backend.deserializer;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.nampi.backend.converter.StringToDateRangeConverter;
import eu.nampi.backend.model.DateRange;

public class DateRangeDeserializer extends StdDeserializer<DateRange> {

  private static final long serialVersionUID = 1L;

  public DateRangeDeserializer() {
    super(String.class);
  }

  @Override
  public DateRange deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    return new StringToDateRangeConverter().convert(p.getText());
  }
}
