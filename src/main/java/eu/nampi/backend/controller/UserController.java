package eu.nampi.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.model.User;
import eu.nampi.backend.repository.UserRepository;

@RestController
public class UserController {

  @Autowired
  UserRepository userRepository;

  @GetMapping("/user")
  @Secured("ROLE_USER")
  public User currentUser() {
    return userRepository.getCurrentUser();
  }
}
