package eu.nampi.backend.model;

import java.util.UUID;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class User {

  @NonNull
  private UUID id;

  @NonNull
  private String username;

  @NonNull
  private String email;

}
