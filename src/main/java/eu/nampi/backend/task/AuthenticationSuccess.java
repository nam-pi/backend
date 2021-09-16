package eu.nampi.backend.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import eu.nampi.backend.model.Author;
import eu.nampi.backend.repository.AuthorRepository;
import eu.nampi.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthenticationSuccess implements ApplicationListener<AuthenticationSuccessEvent> {

  @Autowired
  AuthorRepository authorRepository;

  @Autowired
  UserRepository userRepository;

  @Override
  public void onApplicationEvent(AuthenticationSuccessEvent event) {
    boolean notAuthor = event
        .getAuthentication()
        .getAuthorities()
        .stream()
        .map(a -> a.getAuthority())
        .noneMatch(r -> r.equals("ROLE_AUTHOR"));
    if (notAuthor) {
      return;
    }
    userRepository
        .getCurrentUser()
        .ifPresent(u -> authorRepository
            .findOne(u.getRdfId())
            .ifPresentOrElse(a -> {
              if (!a.getLabel().equals(u.getLabel())) {
                // Label has changed in Keycloak
                Author updated = authorRepository.updateLabel(a, u.getLabel());
                log.debug("Updated label to {} for author {}", u.getLabel(), updated);
              }
            }, () -> {
              // Add new author
              Author newAuthor = authorRepository.addOne(u.getRdfId(), u.getLabel());
              log.debug("Added new author {}", newAuthor);
            }));
  }
}
