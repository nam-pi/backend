package eu.nampi.backend.model;

import java.util.List;
import java.util.UUID;
import org.springframework.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class User {

  @NonNull
  private UUID id;

  @NonNull
  private String username;

  @NonNull
  private String email;

  @NonNull
  private List<String> authorities;

  private String familyName;

  private String givenName;

  private String label;

}
