package eu.nampi.backend.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.nampi.backend.model.OrderByClauses;
import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.repository.PersonRepository;

@RestController
public class PersonController extends AbstractRdfController {

  @Autowired
  PersonRepository personRepository;

  @GetMapping("/persons")
  public void getPersons(@RequestHeader("accept") Lang lang, @RequestParam("page") Optional<Integer> page,
      @RequestParam("pageIndex") Optional<Integer> pageIndex, @RequestParam("limit") Optional<Integer> limit,
      @RequestParam("offset") Optional<Integer> offset, @RequestParam("orderBy") Optional<OrderByClauses> orderBy,
      HttpServletResponse response) {
    QueryParameters params = getParameters(page, pageIndex, limit, offset, orderBy);
    Model model = personRepository.findAll(params);
    writeToOutStream(model, lang, response);
  }
}
