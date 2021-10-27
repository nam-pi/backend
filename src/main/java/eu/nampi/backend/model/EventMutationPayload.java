package eu.nampi.backend.model;

import java.util.List;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import eu.nampi.backend.deserializer.DateRangeDeserializer;
import eu.nampi.backend.deserializer.LiteralDeserializer;
import eu.nampi.backend.deserializer.LiteralListDeserializer;
import eu.nampi.backend.deserializer.ResourceCoupleDeserializer;
import eu.nampi.backend.deserializer.ResourceCoupleListDeserializer;
import eu.nampi.backend.deserializer.ResourceDeserializer;
import eu.nampi.backend.deserializer.ResourceListDeserializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventMutationPayload {

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

  @NotEmpty
  @JsonDeserialize(using = ResourceListDeserializer.class)
  private List<Resource> authors;

  @NotNull
  @JsonDeserialize(using = ResourceDeserializer.class)
  private Resource source;

  @JsonDeserialize(using = LiteralDeserializer.class)
  private Literal sourceLocation;

  @NotNull
  @JsonDeserialize(using = ResourceCoupleDeserializer.class)
  private ResourceCouple mainParticipant;

  @JsonDeserialize(using = ResourceCoupleListDeserializer.class)
  private List<ResourceCouple> otherParticipants;

  @JsonDeserialize(using = ResourceCoupleListDeserializer.class)
  private List<ResourceCouple> aspects;

  @JsonDeserialize(using = ResourceDeserializer.class)
  private Resource place;

  @JsonDeserialize(using = DateRangeDeserializer.class)
  private DateRange date;
}
