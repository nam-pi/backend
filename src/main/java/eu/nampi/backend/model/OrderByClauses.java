package eu.nampi.backend.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.jena.arq.querybuilder.Order;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OrderByClauses {
  private Map<Object, Optional<Order>> orders = new LinkedHashMap<>();

  public void add(Object orderBy, Optional<Order> order) {
    orders.put(orderBy, order);
  }

  public void add(Object orderBy, Order order) {
    orders.put(orderBy, Optional.of(order));
  }

  public void add(Object orderBy) {
    orders.put(orderBy, Optional.empty());
  }

  public void appendAllTo(SelectBuilder selectBuilder) {
    for (Map.Entry<Object, Optional<Order>> entry : this.orders.entrySet()) {
      if (entry.getValue().isPresent()) {
        selectBuilder.addOrderBy(entry.getKey(), entry.getValue().get());
      } else {
        selectBuilder.addOrderBy(entry.getKey());
      }
    }
  }

}
