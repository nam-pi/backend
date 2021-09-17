package eu.nampi.backend.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
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
      @RequestParam("text") Optional<Literal> text) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result = groupRepository.findAll(params, lang);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @GetMapping(value = "/groups/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getEvent(
      @RequestHeader("accept") Lang lang,
      @PathVariable UUID id) {
    String result = groupRepository.findOne(lang, id);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @PostMapping(value = "/groups", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> postgroup(
      @RequestHeader("accept") Lang lang,
      @RequestParam("type") Resource type,
      @RequestParam("label[]") List<Literal> label,
      @RequestParam(value = "comment[]", required = false) List<Literal> comment,
      @RequestParam(value = "text[]", required = false) List<Literal> text,
      @RequestParam(value = "sameAs[]", required = false) List<Resource> sameAs) {
    String newGroup =
        groupRepository.insert(lang, type, label, comment == null ? new ArrayList<>() : comment,
            text == null ? new ArrayList<>() : text, sameAs);
    return new ResponseEntity<String>(newGroup, HttpStatus.CREATED);
  }

  @PutMapping(value = "/groups/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> putgroup(
      @RequestHeader("accept") Lang lang,
      @PathVariable UUID id,
      @RequestParam("type") Resource type,
      @RequestParam("label[]") List<Literal> label,
      @RequestParam(value = "comment[]", required = false) List<Literal> comment,
      @RequestParam(value = "text[]", required = false) List<Literal> text,
      @RequestParam(value = "sameAs[]", required = false) List<Resource> sameAs) {
    String newGroup =
        groupRepository.update(lang, id, type, label, comment == null ? new ArrayList<>() : comment,
            text == null ? new ArrayList<>() : text, sameAs);
    return new ResponseEntity<String>(newGroup, HttpStatus.OK);
  }

  @DeleteMapping(value = "/groups/{id}")
  public ResponseEntity<?> deletegroup(@PathVariable UUID id) {
    groupRepository.delete(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
