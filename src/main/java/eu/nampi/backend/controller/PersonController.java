package eu.nampi.backend.controller;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import eu.nampi.backend.repository.PersonRepository;
import eu.nampi.backend.util.JenaUtils;

@RestController
public class PersonController {

  @Autowired
  PersonRepository personRepository;

  @Autowired
  JenaUtils jenaHelper;

  @GetMapping("/persons/{id}")
  public void getPerson(@RequestHeader("accept") Lang lang, @PathVariable(value = "id") UUID id,
      HttpServletResponse response) {
    Model model = personRepository.getPerson(id);
    jenaHelper.writeModel(model, lang, response);
  }

  @GetMapping("/persons")
  public void getPersons(@RequestHeader("accept") Lang lang, HttpServletResponse response) {
    Model model = personRepository.getPersons();
    jenaHelper.writeModel(model, lang, response);
  }
}
