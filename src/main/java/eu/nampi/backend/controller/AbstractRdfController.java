package eu.nampi.backend.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import eu.nampi.backend.model.OrderByClauses;
import eu.nampi.backend.model.QueryParameters;

public abstract class AbstractRdfController {

  @Value("${nampi.default-limit}")
  int defaultLimit;

  private static final int NO_OFFSET = 0;

  protected QueryParameters getParameters(Optional<Integer> page, Optional<Integer> pageIndex, Optional<Integer> limit,
      Optional<Integer> offset, Optional<OrderByClauses> orderBy) {
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    String baseUrl = request.getRequestURL().toString();
    String relativePath = request.getRequestURI();
    int realLimit = limit.orElse(defaultLimit);
    int realOffset = Optional.ofNullable(page.orElseGet(() -> pageIndex.orElse(null)))
        .map(p -> p * realLimit - realLimit).orElse(NO_OFFSET);
    boolean hasLimit = limit.isPresent();
    OrderByClauses clauses = orderBy.orElse(new OrderByClauses());
    return new QueryParameters(baseUrl, hasLimit, realLimit, realOffset, relativePath, clauses);
  }

}
