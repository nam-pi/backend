package eu.nampi.backend.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.SelectBuilder;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderByClauses implements Serializable {

  private static final long serialVersionUID = 98734561L;

  public static final Order DEFAULT_ORDER = Order.ASCENDING;

  private Map<String, Order> clauses = new LinkedHashMap<>();

  public OrderByClauses(Map<String, Optional<String>> paramMap) {
    for (Map.Entry<String, Optional<String>> param : paramMap.entrySet()) {
      String key = param.getKey();
      if (!key.isEmpty()) {
        String value = param.getValue().orElse("ASC").toUpperCase();
        this.clauses.put(key, value.equals("DESC") ? Order.DESCENDING : Order.ASCENDING);
      }
    }
  }

  public void add(String orderBy, Order order) {
    this.clauses.put(orderBy, order);
  }

  public void add(String orderBy) {
    this.add(orderBy, DEFAULT_ORDER);
  }

  public void appendAllTo(SelectBuilder selectBuilder) {
    for (Map.Entry<String, Order> entry : this.clauses.entrySet()) {
      selectBuilder.addOrderBy(padKey(entry.getKey()), entry.getValue());
    }
  }

  public boolean containsKey(String name) {
    return this.clauses.containsKey(name.startsWith("?") ? name.substring(1) : name);
  }

  public boolean empty() {
    return this.clauses.size() == 0;
  }

  public Optional<Order> getOrderFor(String name) {
    return containsKey(name) ? Optional.of(this.clauses.get(name)) : Optional.empty();
  }

  public Map<String, Order> toMap() {
    return clauses;
  }

  public String toQueryString() {
    return clauses.entrySet().stream().map(e -> e.getKey() + (e.getValue() == Order.ASCENDING ? "" : "=DESC"))
        .collect(Collectors.joining(","));
  }

  public String toString() {
    return "OrderBy[" + clauses.entrySet().stream().map((Map.Entry<String, Order> entry) -> {
      return entry.getKey() + "=" + entry.getValue();
    }).collect(Collectors.joining(";")) + "]";
  }

  private static String padKey(String key) {
    return key.startsWith("?") ? key : "?" + key;
  }
}
