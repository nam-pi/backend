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
import eu.nampi.backend.repository.GroupRepository;

@RestController
public class GroupController extends AbstractRdfController {

  @Autowired
  GroupRepository groupRepository;

  @GetMapping(value = "/groups", produces = { "application/ld+json", "text/turtle", "application/rdf+xml",
      "application/n-triples" })
  public ResponseEntity<String> getGroups(@RequestHeader("accept") Lang lang,
      @RequestParam("page") Optional<Integer> page, @RequestParam("pageIndex") Optional<Integer> pageIndex,
      @RequestParam("limit") Optional<Integer> limit, @RequestParam("offset") Optional<Integer> offset,
      @RequestParam("orderBy") Optional<OrderByClauses> orderBy, Optional<String> type, Optional<String> text) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result = groupRepository.findAll(params, lang);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @GetMapping(value = "/group/{id}", produces = { "application/ld+json", "text/turtle", "application/rdf+xml",
      "application/n-triples" })
  public ResponseEntity<String> getEvent(@RequestHeader("accept") Lang lang, @PathVariable UUID id) {
    String result = groupRepository.findOne(lang, id);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

}
