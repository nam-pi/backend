package eu.nampi.backend.controller;

import javax.servlet.http.HttpServletResponse;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.repository.EventRepository;

@RestController
public class EventController extends AbstractRdfController {

  @Autowired
  EventRepository eventRepository;

  @GetMapping("/events")
  public void getEvents(@RequestHeader("accept") Lang lang,
      @RequestParam(defaultValue = "25") int limit, @RequestParam(defaultValue = "0") int offset,
      HttpServletResponse response) {
    Model model = eventRepository.findAll(limit, offset);
    writeToOutStream(model, lang, response);
  }
}
