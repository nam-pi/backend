package eu.nampi.backend.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderByClauses {

  public static final Order DEFAULT_ORDER = Order.ASCENDING;

  private Map<Object, Order> orders = new LinkedHashMap<>();

  public OrderByClauses(Map<String, Optional<String>> paramMap) {
    for (Map.Entry<String, Optional<String>> param : paramMap.entrySet()) {
      String key = param.getKey();
      String value = param.getValue().orElse("ASC").toUpperCase();
      this.add(key, value.equals("DESC") ? Order.DESCENDING : Order.ASCENDING);
    }
  }

  public void add(Object orderBy, Order order) {
    orders.put(orderBy, order);
  }

  public void add(Object orderBy) {
    orders.put(orderBy, DEFAULT_ORDER);
  }

  public void appendAllTo(SelectBuilder selectBuilder) {
    for (Map.Entry<Object, Order> entry : this.orders.entrySet()) {
      selectBuilder.addOrderBy(entry.getKey(), entry.getValue());
    }
  }

  public Map<Object, Order> toMap() {
    return orders;
  }

  public Optional<Order> getOrderFor(String name) {
    return this.orders.containsKey(name) ? Optional.of(this.orders.get(name)) : Optional.empty();
  }

}
