package eu.nampi.backend.model;

import java.util.Optional;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class ResourceCouple {

  private Optional<Property> predicate;

  @NonNull
  private Resource object;
}

