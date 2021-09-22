package eu.nampi.backend.model;

import java.io.Serializable;
import org.apache.jena.rdf.model.Resource;
import org.springframework.lang.NonNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class InsertResult implements Serializable {

  private static final long serialVersionUID = 1L;

  @NonNull
  Resource entity;

  @NonNull
  String responseBody;
}
