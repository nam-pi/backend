package eu.nampi.backend.converter;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import eu.nampi.backend.model.OrderByClauses;

@Component
public class StringToOrderByClausesConverter implements Converter<String, OrderByClauses> {

  @Override
  public OrderByClauses convert(String orderBy) {
    return new OrderByClauses(splitKeysParam(orderBy));
  }

  private Map<String, Optional<String>> splitKeysParam(String param) {
    return Arrays
        .stream(Optional.ofNullable(param).orElse("").split(","))
        .map(s -> s.split("\\s*=\\s*"))
        .collect(Collectors.toMap(
            a -> a[0],
            a -> a.length == 2 ? Optional.of(a[1]) : Optional.empty(),
            (u, v) -> {
              throw new IllegalStateException(String.format("Duplicate key %s", u));
            }, LinkedHashMap::new));
  }
}
