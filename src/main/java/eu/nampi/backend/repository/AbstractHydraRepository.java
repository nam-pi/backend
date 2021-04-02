package eu.nampi.backend.repository;

import java.io.StringWriter;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.beans.factory.annotation.Autowired;

import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.sparql.InterfaceHydraBuilder;

public abstract class AbstractHydraRepository {

  @Autowired
  private JenaService jenaService;

  protected String serialize(Model model, Lang lang) {
    StringWriter writer = new StringWriter();
    RDFDataMgr.write(writer, model, lang);
    return writer.toString();
  }

  protected Model construct(InterfaceHydraBuilder builder) {
    return jenaService.construct(builder);
  }

}
