package eu.nampi.backend.model;

import java.util.UUID;

import org.springframework.lang.NonNull;

public class User {

  @NonNull
  private UUID id;

  @NonNull
  private String username;

  @NonNull
  private String email;

  public User(UUID id, String username, String email) {
    this.id = id;
    this.username = username;
    this.email = email;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

}
