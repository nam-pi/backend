package eu.nampi.backend.model;

import java.util.UUID;
import org.springframework.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class User {

  @NonNull
  private UUID id;

  @NonNull
  private String username;

  @NonNull
  private String email;

}
