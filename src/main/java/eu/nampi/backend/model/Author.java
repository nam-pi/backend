package eu.nampi.backend.model;

import java.util.UUID;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class Author {

  @NonNull
  String iri;

  @NonNull
  UUID localName;

  @NonNull
  String label;
}
