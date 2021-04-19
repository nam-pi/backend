package eu.nampi.backend.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.model.User;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;
import eu.nampi.backend.vocabulary.SchemaOrg;

@Repository
public class UserRepository extends AbstractHydraRepository {

  public Optional<User> getCurrentUser() {
    Object origPrincipal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (origPrincipal instanceof String) {
      return Optional.empty();
    } else {
      @SuppressWarnings("unchecked")
      KeycloakPrincipal<KeycloakSecurityContext> principal =
          (KeycloakPrincipal<KeycloakSecurityContext>) origPrincipal;
      KeycloakSecurityContext context = principal.getKeycloakSecurityContext();
      AccessToken accessToken = context.getToken();
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      List<String> authorities = new ArrayList<>();
      if (auth != null) {
        authorities =
            auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toList());
      }
      String label = accessToken.getName().isEmpty() ? accessToken.getPreferredUsername()
          : accessToken.getName();
      return Optional.of(new User(UUID.fromString(accessToken.getId()),
          accessToken.getPreferredUsername(), accessToken.getEmail(), authorities,
          accessToken.getFamilyName(), accessToken.getGivenName(), label));
    }

  }

  public Optional<String> getCurrentUser(Lang lang) {
    return getCurrentUser().map(u -> {
      System.out.println(u);
      String uri = endpointUri("user");
      Resource userResource = ResourceFactory.createResource(uri);
      Model model = ModelFactory.createDefaultModel();
      model.setNsPrefix("doc", Doc.getURI()).setNsPrefix("core", Core.getURI());
      model.add(userResource, RDF.type, Doc.user);
      if (u.getAuthorities().contains("ROLE_AUTHOR")) {
        model.add(userResource, RDF.type, Core.author);
        model.add(userResource, SchemaOrg.sameAs,
            ResourceFactory.createResource(individualsUri(Core.author, u.getId())));
      }
      model.add(userResource, RDFS.label, u.getLabel());
      model.add(userResource, SchemaOrg.givenName, u.getGivenName());
      model.add(userResource, SchemaOrg.familyName, u.getFamilyName());
      model.add(userResource, SchemaOrg.email, u.getEmail());
      model.add(userResource, SchemaOrg.name, u.getUsername());
      model.add(userResource, SchemaOrg.identifier, u.getId().toString());
      return serialize(model, lang, userResource);
    });
  }
}
