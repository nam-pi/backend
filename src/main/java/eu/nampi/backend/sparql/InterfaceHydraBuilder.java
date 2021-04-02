package eu.nampi.backend.sparql;

import org.apache.jena.query.Query;

public interface InterfaceHydraBuilder {

  public Query build();

  public String buildString();

}
