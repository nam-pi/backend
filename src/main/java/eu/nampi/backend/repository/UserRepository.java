package eu.nampi.backend.repository;

import java.util.UUID;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.model.User;

@Repository
public class UserRepository {

  public User getCurrentUser() {
    @SuppressWarnings("unchecked")
    KeycloakPrincipal<KeycloakSecurityContext> principal = (KeycloakPrincipal<KeycloakSecurityContext>) SecurityContextHolder
        .getContext().getAuthentication().getPrincipal();
    KeycloakSecurityContext context = principal.getKeycloakSecurityContext();
    AccessToken accessToken = context.getToken();
    return new User(UUID.fromString(accessToken.getId()), accessToken.getName(), accessToken.getEmail());
  }
}
