package eu.nampi.backend.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import eu.nampi.backend.model.CollectionMeta;
import eu.nampi.backend.model.OrderByClauses;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractRdfController {

  @Value("${nampi.default-limit}")
  int defaultLimit;

  private static final int DEFAULT_OFFSET = 0;

  protected void writeToOutStream(Model model, Lang lang, HttpServletResponse response) {
    try {
      RDFDataMgr.write(response.getOutputStream(), model, lang);
    } catch (IOException e) {
      response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
      log.error(e.getMessage());
    }
  }

  /**
   * Splits a list/keys based query parameter string into a map of the sub-parameter name and value
   * (if any).
   * 
   * @param param The query parameter string, for instance "foo=1,bar=2", "foo,bar" or "foo,bar=1"
   * @return An ordered map with all key-value pairs. The value can be an empty Optional if there
   *         isn't a value in the parameter string
   */
  private Map<String, Optional<String>> splitKeysParam(String param) {
    return Arrays.stream(Optional.ofNullable(param).orElse("").split(","))
        .map(s -> s.split("\\s*=\\s*")).collect(Collectors.toMap(a -> a[0],
            a -> a.length == 2 ? Optional.of(a[1]) : Optional.empty(), (u, v) -> {
              throw new IllegalStateException(String.format("Duplicate key %s", u));
            }, LinkedHashMap::new));
  }

  protected CollectionMeta getCollectionMeta() {
    HttpServletRequest request =
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    Optional<Integer> page =
        Optional.ofNullable(request.getParameter("page")).map(Integer::valueOf);
    Optional<Integer> pageIndex =
        Optional.ofNullable(request.getParameter("pageIndex")).map(Integer::valueOf);
    Optional<Integer> offset =
        Optional.ofNullable(request.getParameter("offset")).map(Integer::valueOf);
    Optional<Integer> limit =
        Optional.ofNullable(request.getParameter("limit")).map(Integer::valueOf);
    int effectiveLimit = limit.orElse(defaultLimit);
    int effectiveOffset = Optional.ofNullable(page.orElseGet(() -> pageIndex.orElse(null)))
        .map(p -> (p * effectiveLimit) - effectiveLimit).orElse(offset.orElse(DEFAULT_OFFSET));
    OrderByClauses clauses = new OrderByClauses(splitKeysParam(request.getParameter("orderBy")));
    return new CollectionMeta(request.getRequestURL().toString(), limit.isPresent(), effectiveLimit,
        effectiveOffset, request.getRequestURI(), clauses);
  }

}
