package eu.nampi.backend.controller;

import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.repository.DocumentationRepository;

@RestController
public class DocumentationController extends AbstractRdfController {

  @Autowired
  DocumentationRepository documentationRepository;

  @GetMapping(value = "/doc", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getDocumentation(@RequestHeader("accept") Lang lang) {
    return new ResponseEntity<String>(documentationRepository.get(lang), HttpStatus.OK);
  }

}
