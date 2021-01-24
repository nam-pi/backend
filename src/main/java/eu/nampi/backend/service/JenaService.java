package eu.nampi.backend.service;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

public interface JenaService {

  public Model construct(String query, boolean useInference);

  public ResultSet select(String query, boolean useInference);

}
