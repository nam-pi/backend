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
import eu.nampi.backend.repository.EventRepository;

@RestController
public class EventController extends AbstractRdfController {

  @Autowired
  EventRepository eventRepository;

  @GetMapping(value = "/events", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getEvents(@RequestHeader("accept") Lang lang,
      @RequestParam("page") Optional<Integer> page,
      @RequestParam("pageIndex") Optional<Integer> pageIndex,
      @RequestParam("limit") Optional<Integer> limit,
      @RequestParam("offset") Optional<Integer> offset,
      @RequestParam("orderBy") Optional<OrderByClauses> orderBy,
      @RequestParam("type") Optional<String> type, @RequestParam("text") Optional<String> text,
      @RequestParam("dates") Optional<String> dates,
      @RequestParam("aspect") Optional<String> aspect,
      @RequestParam("aspectType") Optional<String> aspectType,
      @RequestParam("aspectUseType") Optional<String> aspectUseType,
      @RequestParam("participant") Optional<String> participant,
      @RequestParam("participantType") Optional<String> participantType,
      @RequestParam("participationType") Optional<String> participationType,
      @RequestParam("place") Optional<String> place,
      @RequestParam("author") Optional<String> author) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result = eventRepository.findAll(params, lang, dates, aspect, aspectType, aspectUseType,
        participant, participantType, participationType, place, author);
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
