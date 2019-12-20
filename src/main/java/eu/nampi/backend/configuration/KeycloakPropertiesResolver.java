package eu.nampi.backend.configuration;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakPropertiesResolver extends KeycloakSpringBootConfigResolver {
  private final KeycloakDeployment keycloakDeployment;

  public KeycloakPropertiesResolver(KeycloakSpringBootProperties properties) {
    keycloakDeployment = KeycloakDeploymentBuilder.build(properties);
  }

  @Override
  public KeycloakDeployment resolve(HttpFacade.Request facade) {
    return keycloakDeployment;
  }
}
