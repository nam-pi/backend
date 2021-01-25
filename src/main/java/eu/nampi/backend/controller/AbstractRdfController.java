package eu.nampi.backend.controller;

import java.io.IOException;
import java.util.Optional;
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
    return new CollectionMeta(request.getRequestURL().toString(), limit.isPresent(), effectiveLimit,
        effectiveOffset);
  }

}
