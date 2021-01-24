package eu.nampi.backend.controller;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRdfController {

  protected void writeToOutStream(Model model, Lang lang, HttpServletResponse response) {
    try {
      RDFDataMgr.write(response.getOutputStream(), model, lang);
    } catch (IOException e) {
      response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
    }
  }

}
