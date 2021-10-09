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
import eu.nampi.backend.repository.PlaceRepository;

@RestController
public class PlaceController extends AbstractRdfController {

  @Autowired
  PlaceRepository placeRepository;

  @GetMapping(value = "/places", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getPlaces(
      @RequestHeader("accept") Lang lang,
      @RequestParam("page") Optional<Integer> page,
      @RequestParam("pageIndex") Optional<Integer> pageIndex,
      @RequestParam("limit") Optional<Integer> limit,
      @RequestParam("offset") Optional<Integer> offset,
      @RequestParam("orderBy") Optional<OrderByClauses> orderBy,
      @RequestParam("type") Optional<Resource> type,
      @RequestParam("text") Optional<Literal> text) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy, type, text);
    String result = placeRepository.findAll(params, lang);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @GetMapping(value = "/places/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> getEvent(
      @RequestHeader("accept") Lang lang,
      @PathVariable UUID id) {
    String result = placeRepository.findOne(lang, id);
    return new ResponseEntity<String>(result, HttpStatus.OK);
  }

  @PostMapping(value = "/places", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> postPlace(
      @RequestHeader("accept") Lang lang,
      @RequestParam("type") Resource type,
      @RequestParam("label[]") List<Literal> labels,
      @RequestParam(value = "comment[]", required = false) List<Literal> comments,
      @RequestParam(value = "text[]", required = false) List<Literal> texts,
      @RequestParam(value = "sameAs[]", required = false) List<Resource> sameAs,
      @RequestParam(value = "latitude", required = false) Optional<Double> latitude,
      @RequestParam(value = "longitude", required = false) Optional<Double> longitude) {
    InsertResult result =
        placeRepository.insert(lang, type, labels, asList(comments), asList(texts), asList(sameAs),
            latitude, longitude);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Location", result.getEntity().getURI());
    return new ResponseEntity<String>(result.getResponseBody(), headers, HttpStatus.CREATED);
  }

  @PutMapping(value = "/places/{id}", produces = {"application/ld+json", "text/turtle",
      "application/rdf+xml", "application/n-triples"})
  public ResponseEntity<String> putPlace(
      @RequestHeader("accept") Lang lang,
      @PathVariable UUID id,
      @RequestParam("type") Resource type,
      @RequestParam("label[]") List<Literal> labels,
      @RequestParam(value = "comment[]", required = false) List<Literal> comments,
      @RequestParam(value = "text[]", required = false) List<Literal> texts,
      @RequestParam(value = "sameAs[]", required = false) List<Resource> sameAs,
      @RequestParam(value = "latitude", required = false) Optional<Double> latitude,
      @RequestParam(value = "longitude", required = false) Optional<Double> longitude) {
    String newPlace =
        placeRepository.update(lang, id, type, labels, asList(comments), asList(texts),
            asList(sameAs), latitude, longitude);
    return new ResponseEntity<String>(newPlace, HttpStatus.OK);
  }

  @DeleteMapping(value = "/places/{id}")
  public ResponseEntity<?> deletePlace(@PathVariable UUID id) {
    placeRepository.delete(id);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
