package eu.nampi.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollectionMeta {

  private String baseUrl;

  private boolean customLimit;

  private int limit;

  private int offset;

  private String relativePath;

}
