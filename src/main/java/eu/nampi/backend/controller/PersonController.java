package eu.nampi.backend.controller;

import javax.servlet.http.HttpServletResponse;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import eu.nampi.backend.repository.PersonRepository;
import eu.nampi.backend.service.JenaService;

@RestController
public class PersonController {

  @Autowired
  PersonRepository personRepository;

  @Autowired
  JenaService service;

  @GetMapping("/persons")
  public void getPersons(@RequestHeader("accept") Lang lang, HttpServletResponse response) {
    Model model = personRepository.findAll();
    service.writeToOutStream(model, lang, response);
  }
}
