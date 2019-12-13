package eu.nampi.backend.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

  @GetMapping("/admin/hello")
  @Secured("ROLE_ADMIN")
  public String sayHelloToAdmin() {
    return "Hello Admin";
  }

  @GetMapping("/user/hello")
  @Secured("ROLE_USER")
  public String sayHelloToUser() {
    return "Hello User";
  }

}
