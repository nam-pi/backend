package eu.nampi.backend.controller;

import org.apache.jena.riot.Lang;
import org.apache.jena.shared.AccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.repository.UserRepository;

@RestController
public class UserController {

  @Autowired
  UserRepository userRepository;

  @GetMapping(value = "/user",
      produces = {"application/ld+json", "text/turtle", "application/rdf+xml",
          "application/n-triples"})
  @Secured("ROLE_USER")
  public ResponseEntity<String> currentUser(@RequestHeader("accept") Lang lang) {
    return userRepository.getCurrentUser(lang)
        .map(result -> new ResponseEntity<String>(result, HttpStatus.OK))
        .orElseThrow(AccessDeniedException::new);

  }
}
