package eu.nampi.backend.controller;

import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.exception.ForbiddenException;
import eu.nampi.backend.model.EventMutationPayload;
import eu.nampi.backend.model.InsertResult;
import eu.nampi.backend.model.OrderByClauses;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.repository.EventRepository;
import eu.nampi.backend.repository.UserRepository;

@RestController
public class EventController extends AbstractRdfController {

  @Autowired
  EventRepository eventRepository;

  @Autowired
  UserRepository userRepository;

  @GetMapping(value = "/events", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getEvents(
      @RequestHeader("accept") Lang lang,
      @RequestParam("page") Optional<Integer> page,
      @RequestParam("pageIndex") Optional<Integer> pageIndex,
      @RequestParam("limit") Optional<Integer> limit,
      @RequestParam("offset") Optional<Integer> offset,
      @RequestParam("orderBy") Optional<OrderByClauses> orderBy,
      @RequestParam("type") Optional<Resource> type,
      @RequestParam("text") Optional<Literal> text,
      @RequestParam("dates") Optional<String> dates,
      @RequestParam("aspect") Optional<Resource> aspect,
      @RequestParam("aspectType") Optional<Resource> aspectType,
      @RequestParam("aspectUseType") Optional<Property> aspectUseType,
      @RequestParam("participant") Optional<Resource> participant,
      @RequestParam("participantType") Optional<Resource> participantType,
      @RequestParam("participationType") Optional<Property> participationType,
      @RequestParam("place") Optional<Resource> place,
      @RequestParam("author") Optional<Resource> author) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result =
        eventRepository.findAll(params, lang, dates, aspect, aspectType, aspectUseType, participant,
            participantType, participationType, place, author);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @GetMapping(value = "/events/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getEvent(@RequestHeader("accept") Lang lang,
      @PathVariable UUID id) {
    String result = eventRepository.findOne(lang, id);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @PostMapping(value = "/events", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"}, consumes = {"application/json"})
  public ResponseEntity<String> postEvent(
      @RequestHeader("accept") Lang lang,
      @Valid @RequestBody EventMutationPayload payload) {
    InsertResult result = eventRepository.insert(lang, payload.getTypes(), payload.getLabels(),
        asList(payload.getComments()), asList(payload.getTexts()), payload.getAuthors(),
        payload.getSource(), payload.getSourceLocation(), payload.getMainParticipant(),
        asList(payload.getOtherParticipants()), asList(payload.getAspects()),
        Optional.ofNullable(payload.getPlace()), Optional.ofNullable(payload.getDate()));
    HttpHeaders headers = new HttpHeaders();
    headers.add("Location", result.getEntity().getURI());
    return new ResponseEntity<String>(result.getResponseBody(), headers, HttpStatus.CREATED);
  }

  @PutMapping(value = "/events/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"}, consumes = {"application/json"})
  public ResponseEntity<String> putEvent(
      @PathVariable("id") UUID id,
      @RequestHeader("accept") Lang lang,
      @Valid @RequestBody EventMutationPayload payload) {
    UUID userId = userRepository.getCurrentUser().map(user -> user.getRdfId()).orElseThrow();
    if (!eventRepository.isAuthor(userId, id)) {
      throw new ForbiddenException();
    }
    String newEvent = eventRepository.update(lang, id, payload.getTypes(), payload.getLabels(),
        asList(payload.getComments()), asList(payload.getTexts()), payload.getAuthors(),
        payload.getSource(), payload.getSourceLocation(), payload.getMainParticipant(),
        asList(payload.getOtherParticipants()), asList(payload.getAspects()),
        Optional.ofNullable(payload.getPlace()), Optional.ofNullable(payload.getDate()));
    return new ResponseEntity<String>(newEvent, HttpStatus.CREATED);
  }

  @DeleteMapping(value = "/events/{id}")
  public ResponseEntity<?> deleteEvent(@PathVariable UUID id) {
    UUID userId = userRepository.getCurrentUser().map(user -> user.getRdfId()).orElseThrow();
    if (!eventRepository.isAuthor(userId, id)) {
      throw new ForbiddenException();
    }
    eventRepository.delete(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
