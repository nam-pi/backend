package eu.nampi.backend.controller;

import java.util.Optional;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.model.OrderByClauses;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.repository.ClassRepository;

@RestController
public class ClassController extends AbstractRdfController {

  @Autowired
  ClassRepository classRepository;

  @GetMapping(value = "/classes", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getPlaces(@RequestHeader("accept") Lang lang,
      @RequestParam("page") Optional<Integer> page,
      @RequestParam("pageIndex") Optional<Integer> pageIndex,
      @RequestParam("limit") Optional<Integer> limit,
      @RequestParam("offset") Optional<Integer> offset,
      @RequestParam("orderBy") Optional<OrderByClauses> orderBy,
      @RequestParam("type") Optional<String> type, @RequestParam("text") Optional<String> text,
      @RequestParam("parent") Optional<String> parent) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result = classRepository.findAll(params, lang, parent);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

}
