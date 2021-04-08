package eu.nampi.backend.controller;

import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import eu.nampi.backend.repository.EntrypointRepository;

@RestController
public class EntrypointController extends AbstractRdfController {

  @Autowired
  EntrypointRepository entrypointRepository;

  @GetMapping("/")
  public ResponseEntity<String> getEntryPoint(@RequestHeader("accept") Lang lang) {
    return new ResponseEntity<String>(entrypointRepository.get(lang), HttpStatus.OK);
  }

}
