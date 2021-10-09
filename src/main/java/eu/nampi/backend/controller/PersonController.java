package eu.nampi.backend.controller;

import java.util.ArrayList;
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
import eu.nampi.backend.repository.PersonRepository;

@RestController
public class PersonController extends AbstractRdfController {

  @Autowired
  PersonRepository personRepository;

  @GetMapping(value = "/persons", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getPersons(
      @RequestHeader("accept") Lang lang,
      @RequestParam("page") Optional<Integer> page,
      @RequestParam("pageIndex") Optional<Integer> pageIndex,
      @RequestParam("limit") Optional<Integer> limit,
      @RequestParam("offset") Optional<Integer> offset,
      @RequestParam("orderBy") Optional<OrderByClauses> orderBy,
      @RequestParam("type") Optional<Resource> type,
      @RequestParam("text") Optional<Literal> text,
      @RequestParam("aspect") Optional<Resource> aspect) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result = personRepository.findAll(params, lang, aspect);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @GetMapping(value = "/persons/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getEvent(
      @RequestHeader("accept") Lang lang,
      @PathVariable UUID id) {
    String result = personRepository.findOne(lang, id);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @PostMapping(value = "/persons", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> postPerson(
      @RequestHeader("accept") Lang lang,
      @RequestParam("type") Resource type,
      @RequestParam("label[]") List<Literal> labels,
      @RequestParam(value = "comment[]", required = false) List<Literal> comments,
      @RequestParam(value = "text[]", required = false) List<Literal> texts,
      @RequestParam(value = "sameAs[]", required = false) List<Resource> sameAs) {
    InsertResult result =
        personRepository.insert(lang, type, labels, asList(comments), asList(texts),
            asList(sameAs));
    HttpHeaders headers = new HttpHeaders();
    headers.add("Location", result.getEntity().getURI());
    return new ResponseEntity<String>(result.getResponseBody(), headers, HttpStatus.CREATED);
  }

  @PutMapping(value = "/persons/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> putPerson(
      @RequestHeader("accept") Lang lang,
      @PathVariable UUID id,
      @RequestParam("type") Resource type,
      @RequestParam("label[]") List<Literal> labels,
      @RequestParam(value = "comment[]", required = false) List<Literal> comments,
      @RequestParam(value = "text[]", required = false) List<Literal> texts,
      @RequestParam(value = "sameAs[]", required = false) List<Resource> sameAs) {
    String newPerson = personRepository.update(lang, id, type, labels,
        comments == null ? new ArrayList<>() : asList(comments), asList(texts), asList(sameAs));
    return new ResponseEntity<String>(newPerson, HttpStatus.OK);
  }

  @DeleteMapping(value = "/persons/{id}")
  public ResponseEntity<?> deletePerson(@PathVariable UUID id) {
    personRepository.delete(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
