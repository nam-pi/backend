package eu.nampi.backend.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import eu.nampi.backend.repository.EventRepository;
import eu.nampi.backend.util.JenaUtils;

@RestController
public class EventController {

  @Autowired
  EventRepository eventRepository;

  @Autowired
  JenaUtils jenaHelper;

  @GetMapping("/events")
  public void getEvents(@RequestHeader("accept") Lang lang, HttpServletResponse response) {
    Model model = eventRepository.getEvents();
    jenaHelper.writeModel(model, lang, response);
  }
}
