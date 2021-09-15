package eu.nampi.backend.model.hydra;

import org.springframework.http.HttpMethod;
import eu.nampi.backend.vocabulary.Hydra;

public class SupportedOperation extends AbstractHydraNode {

  public SupportedOperation(String title, HttpMethod method) {
    super(title, Hydra.Operation);
    init(method);
  }

  public SupportedOperation(String idUrl, String title, HttpMethod method) {
    super(idUrl, title, Hydra.Operation);
    init(method);
  }

  private void init(HttpMethod method) {
    add(Hydra.method, method.name());
  }
}
