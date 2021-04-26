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
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.model.OrderByClauses;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.repository.EventRepository;

@RestController
public class EventController extends AbstractRdfController {

  @Autowired
  EventRepository eventRepository;

  @GetMapping(value = "/events", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getEvents(@RequestHeader("accept") Lang lang,
      Optional<Integer> page, Optional<Integer> pageIndex, Optional<Integer> limit,
      Optional<Integer> offset, Optional<OrderByClauses> orderBy, Optional<String> type,
      Optional<String> text, Optional<String> dates, Optional<String> aspect,
      Optional<String> aspectType, Optional<String> aspectUseType, Optional<String> participant,
      Optional<String> participantType, Optional<String> participationType,
      Optional<String> place) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result = eventRepository.findAll(params, lang, dates, aspect, aspectType, aspectUseType,
        participant, participantType, participationType, place);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @GetMapping(value = "/event/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getEvent(@RequestHeader("accept") Lang lang,
      @PathVariable UUID id) {
    String result = eventRepository.findOne(lang, id);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

}
