package eu.nampi.backend.service;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.rdf.model.Model;

public interface JenaService {

  public Model construct(ConstructBuilder constructBuilder);

  public void clearGraph(String uri);

  public void replaceGraph(String graphUri, Model model);

}
