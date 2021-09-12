package eu.nampi.backend.model;

import java.io.Serializable;
import java.util.Optional;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class QueryParameters implements Serializable {
  private static final long serialVersionUID = 12327123L;

  private String baseUrl;

  private boolean customLimit;

  private int limit;

  private int offset;

  private String relativePath;

  private OrderByClauses orderByClauses;

  private Optional<Resource> type;

  private Optional<Literal> text;

}
