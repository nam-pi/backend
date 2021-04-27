package eu.nampi.backend.controller;

import java.util.Optional;
import java.util.UUID;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.model.OrderByClauses;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.repository.AspectRepository;

@RestController
public class AspectController extends AbstractRdfController {

  @Autowired
  AspectRepository aspectRepository;

  @GetMapping(value = "/aspects", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getAspects(@RequestHeader("accept") Lang lang,
      @RequestParam("page") Optional<Integer> page,
      @RequestParam("pageIndex") Optional<Integer> pageIndex,
      @RequestParam("limit") Optional<Integer> limit,
      @RequestParam("offset") Optional<Integer> offset,
      @RequestParam("orderBy") Optional<OrderByClauses> orderBy,
      @RequestParam("type") Optional<String> type, @RequestParam("text") Optional<String> text,
      @RequestParam("person") Optional<String> person) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result = aspectRepository.findAll(params, lang, person);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @GetMapping(value = "/aspect/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getAspect(@RequestHeader("accept") Lang lang,
      @PathVariable UUID id) {
    String result = aspectRepository.findOne(lang, id);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }
}
