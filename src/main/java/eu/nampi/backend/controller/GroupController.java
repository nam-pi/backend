package eu.nampi.backend.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.model.InsertResult;
import eu.nampi.backend.model.OrderByClauses;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.repository.GroupRepository;

@RestController
public class GroupController extends AbstractRdfController {

  @Autowired
  GroupRepository groupRepository;

  @GetMapping(value = "/groups", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getGroups(
      @RequestHeader("accept") Lang lang,
      @RequestParam("page") Optional<Integer> page,
      @RequestParam("pageIndex") Optional<Integer> pageIndex,
      @RequestParam("limit") Optional<Integer> limit,
      @RequestParam("offset") Optional<Integer> offset,
      @RequestParam("orderBy") Optional<OrderByClauses> orderBy,
      @RequestParam("type") Optional<Resource> type,
      @RequestParam("text") Optional<Literal> text,
      @RequestParam("partOf") Optional<Resource> partOf,
      @RequestParam("hasPart") Optional<Resource> hasPart) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result = groupRepository.findAll(params, lang, partOf, hasPart);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @GetMapping(value = "/groups/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getGvent(
      @RequestHeader("accept") Lang lang,
      @PathVariable UUID id) {
    String result = groupRepository.findOne(lang, id);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @PostMapping(value = "/groups", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> postGroup(
      @RequestHeader("accept") Lang lang,
      @RequestParam("type") Resource type,
      @RequestParam("labels[]") List<Literal> labels,
      @RequestParam(value = "comments[]", required = false) List<Literal> comments,
      @RequestParam(value = "texts[]", required = false) List<Literal> texts,
      @RequestParam(value = "sameAs[]", required = false) List<Resource> sameAs,
      @RequestParam(value = "partOf[]", required = false) List<Resource> partOfs) {
    InsertResult result =
        groupRepository.insert(lang, type, labels, asList(comments), asList(texts),
            asList(sameAs), asList(partOfs));
    HttpHeaders headers = new HttpHeaders();
    headers.add("Location", result.getEntity().getURI());
    return new ResponseEntity<String>(result.getResponseBody(), headers, HttpStatus.CREATED);
  }

  @PutMapping(value = "/groups/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> putGroup(
      @RequestHeader("accept") Lang lang,
      @PathVariable UUID id,
      @RequestParam("type") Resource type,
      @RequestParam("labels[]") List<Literal> labels,
      @RequestParam(value = "comments[]", required = false) List<Literal> comments,
      @RequestParam(value = "texts[]", required = false) List<Literal> texts,
      @RequestParam(value = "sameAs[]", required = false) List<Resource> sameAs,
      @RequestParam(value = "partOf[]", required = false) List<Resource> partOfs) {
    String newGroup =
        groupRepository.update(lang, id, type, labels, asList(comments), asList(texts),
            asList(sameAs), asList(partOfs));
    return new ResponseEntity<String>(newGroup, HttpStatus.OK);
  }

  @DeleteMapping(value = "/groups/{id}")
  public ResponseEntity<?> deleteGroup(@PathVariable UUID id) {
    groupRepository.delete(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
