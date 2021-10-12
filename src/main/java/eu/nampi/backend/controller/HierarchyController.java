package eu.nampi.backend.controller;

import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.repository.HierarchyRepository;

@RestController
public class HierarchyController extends AbstractRdfController {

  @Autowired
  HierarchyRepository hierarchyRepository;

  @GetMapping(value = "/hierarchy", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getHierarchy(
      @RequestHeader("accept") Lang lang,
      @RequestParam("iri") String iri,
      @RequestParam(value = "descendants", required = false) Boolean descendants) {
    String result =
        hierarchyRepository.findHierarchy(lang, iri, descendants == null ? false : descendants);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }
}
