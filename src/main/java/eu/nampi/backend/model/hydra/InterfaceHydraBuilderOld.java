package eu.nampi.backend.model.hydra;

import org.apache.jena.query.Query;

public interface InterfaceHydraBuilderOld {

  public Query build();

  public String buildString();

}
