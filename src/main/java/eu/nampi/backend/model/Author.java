package eu.nampi.backend.model;

import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
public class Author implements Serializable {

  private static final long serialVersionUID = 1L;

  @NonNull
  String iri;

  @NonNull
  UUID localName;

  @NonNull
  String label;
}
