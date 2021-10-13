package eu.nampi.backend.model;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import eu.nampi.backend.vocabulary.Hydra;

public class ParameterMapper {

  public enum IriTarget {
    FIRST, LAST, NEXT, PREVIOUS, IRI
  }

  private final String baseUrl;
  private final Resource collection;
  private final List<String> parts = new ArrayList<>();
  Map<String, String> viewParts = new LinkedHashMap<>();
  private Model model;
  private Resource resSearch = ResourceFactory.createResource();

  public ParameterMapper(String baseUrl, Resource collection, Model model) {
    this.model = model;
    this.baseUrl = baseUrl;
    this.collection = collection;
    this.model
        .add(collection, Hydra.search, resSearch)
        .add(resSearch, RDF.type, Hydra.IriTemplate)
        .add(resSearch, Hydra.variableRepresentation, Hydra.BasicRepresentation);
  }

  public ParameterMapper add(String variable, Resource type, Optional<?> value, boolean required) {
    Resource mapping = ResourceFactory.createResource();
    this.parts
        .add(variable);
    this.model
        .add(resSearch, Hydra.mapping, mapping)
        .add(mapping, RDF.type, Hydra.IriTemplateMapping)
        .add(mapping, Hydra.variable, variable)
        .add(mapping, Hydra.property, type)
        .addLiteral(mapping, Hydra.required, required);
    value
        .map(String::valueOf)
        .map(String::trim)
        .filter(str -> !str.isEmpty())
        .map(str -> URLEncoder.encode(str, Charset.defaultCharset()))
        .ifPresent(encoded -> viewParts.put(variable, encoded));
    return this;
  }

  public ParameterMapper add(String variable, Resource type, Optional<?> value) {
    return this.add(variable, type, value, false);
  }

  public ParameterMapper add(String variable, Resource type, Object value, boolean required) {
    return this.add(variable, type, Optional.ofNullable(value), required);
  }

  public ParameterMapper add(String variable, Resource type, Object value) {
    return this.add(variable, type, Optional.ofNullable(value));
  }

  public ParameterMapper insertTemplate() {
    StringBuilder templateBuilder = new StringBuilder(this.baseUrl);
    if (!this.parts.isEmpty()) {
      templateBuilder.append(parts.stream().sorted().collect(Collectors.joining(",", "{?", "}")));
    }
    this.model.add(resSearch, Hydra.template, templateBuilder.toString());
    return this;
  }

  public void insertView(int totalItems) {
    Resource view = ResourceFactory.createResource(viewIri(totalItems, IriTarget.IRI).get());
    this.model
        .add(this.collection, Hydra.view, view)
        .add(view, RDF.type, Hydra.PartialCollectionView);
    viewIri(totalItems, IriTarget.FIRST).map(ResourceFactory::createResource)
        .ifPresent(first -> this.model.add(view, Hydra.first, first));
    viewIri(totalItems, IriTarget.LAST).map(ResourceFactory::createResource)
        .ifPresent(last -> this.model.add(view, Hydra.last, last));
    viewIri(totalItems, IriTarget.NEXT).map(ResourceFactory::createResource)
        .ifPresent(next -> this.model.add(view, Hydra.next, next));
    viewIri(totalItems, IriTarget.PREVIOUS).map(ResourceFactory::createResource)
        .ifPresent(previous -> this.model.add(view, Hydra.previous, previous));
  }

  private Optional<String> viewIri(int totalItems, IriTarget target) {
    Map<String, String> map = copy(viewParts);
    if (target != IriTarget.IRI) {
      Optional<String> newOffset = replaceOffset(totalItems, target);
      if (newOffset.isEmpty()) {
        return Optional.empty();
      }
      map.replace("offset", newOffset.get());
    }
    StringBuilder viewStringBuilder = new StringBuilder(this.baseUrl);
    if (!map.isEmpty()) {
      viewStringBuilder.append(map.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue())
          .sorted().collect(Collectors.joining("&", "?", "")));
    }
    return Optional.of(viewStringBuilder.toString());
  }

  private Optional<String> replaceOffset(int totalItems, IriTarget target) {
    int offset = Optional.ofNullable(viewParts.get("offset")).map(Integer::valueOf).orElse(0);
    int limit = Optional.ofNullable(viewParts.get("limit")).map(Integer::valueOf).orElse(25);
    if (totalItems <= limit) {
      return Optional.empty();
    }
    int nextOffset = offset + limit;
    int lastOffset = (int) (Math.floor(totalItems / limit) * limit);
    if (lastOffset == totalItems) {
      lastOffset = totalItems - limit;
    }
    int previousOffset = offset - limit;
    if (target == IriTarget.FIRST) {
      if (offset == 0) {
        return Optional.empty();
      } else {
        return Optional.of("0");
      }
    } else if (target == IriTarget.LAST) {
      if (offset == lastOffset) {
        return Optional.empty();
      } else {
        return Optional.of(String.valueOf(lastOffset));
      }
    } else if (target == IriTarget.NEXT) {
      if (nextOffset >= totalItems) {
        return Optional.empty();
      } else {
        return Optional.of(String.valueOf(nextOffset));
      }
    } else if (target == IriTarget.PREVIOUS) {
      if (previousOffset < 0) {
        return Optional.empty();
      } else {
        return Optional.of(String.valueOf(previousOffset));
      }
    }
    return Optional.empty();
  }

  public Map<String, String> copy(Map<String, String> originalMap) {
    Set<Entry<String, String>> entries = originalMap.entrySet();
    return (HashMap<String, String>) entries.stream()
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
