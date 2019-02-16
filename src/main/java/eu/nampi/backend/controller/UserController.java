package eu.nampi.backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import eu.nampi.backend.ModelSerializer;
import eu.nampi.backend.exception.UserNotFoundException;
import eu.nampi.backend.model.RequestBodyCreateUser;
import eu.nampi.backend.model.User;
import eu.nampi.backend.service.UserService;

@RestController
@RequestMapping("/users")
class ResourceController {

    @NonNull
    private final UserService userService;

    public ResourceController(UserService userService) {
        this.userService = userService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public String postUser(@RequestBody RequestBodyCreateUser body) {
        User user = userService.createUser(body.getUserName(), body.getEmail());
        return user.toResource().getURI();
    }

    @GetMapping(params = "email")
    public ResponseEntity<String> getUserByEmail(@RequestParam("email") String email,
            @RequestHeader(name = HttpHeaders.CONTENT_TYPE, defaultValue = "application/ld+json") String contentType) {
        return userService.findByEmail(email).map(User::toModel)
                .map(model -> ModelSerializer.forContentType(model, contentType))
                .map(body -> ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, contentType).body(body))
                .orElseThrow(UserNotFoundException::new);
    }

}