package eu.nampi.backend.model;

import org.springframework.http.HttpMethod;
import eu.nampi.backend.vocabulary.Hydra;

public class HydraSupportedOperation extends AbstractHydraNode {

  public HydraSupportedOperation(String title, HttpMethod method) {
    super(title, Hydra.Operation);
    init(method);
  }

  public HydraSupportedOperation(String idUrl, String title, HttpMethod method) {
    super(idUrl, title, Hydra.Operation);
    init(method);
  }

  private void init(HttpMethod method) {
    add(Hydra.method, method.name());
  }
}
