package eu.nampi.backend.model;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import eu.nampi.backend.deserializer.LiteralListDeserializer;
import eu.nampi.backend.deserializer.ResourceListDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceMutationPayload {

  @NotEmpty
  @JsonDeserialize(using = ResourceListDeserializer.class)
  private List<Resource> types;

  @NotEmpty
  @JsonDeserialize(using = LiteralListDeserializer.class)
  private List<Literal> labels;

  @JsonDeserialize(using = LiteralListDeserializer.class)
  private List<Literal> comments;

  @JsonDeserialize(using = LiteralListDeserializer.class)
  private List<Literal> texts;

  @JsonDeserialize(using = ResourceListDeserializer.class)
  private List<Resource> sameAs;

  private Double latitude;

  private Double logitude;
}
