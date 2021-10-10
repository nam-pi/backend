package eu.nampi.backend.configuration;

import java.util.Objects;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import eu.nampi.backend.converter.StringToLangConverter;
import eu.nampi.backend.exception.DeletionNotPermittedException;
import eu.nampi.backend.exception.ForbiddenException;
import eu.nampi.backend.exception.NotFoundException;
import eu.nampi.backend.util.Serializer;
import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.SchemaOrg;

@ControllerAdvice
public class HydraControllerAdvice extends ResponseEntityExceptionHandler {

  @Autowired
  Serializer serializer;

  @ExceptionHandler(value = NotFoundException.class)
  protected ResponseEntity<Object> handleNotFound(RuntimeException ex, WebRequest request) {
    return handle(ex, request, HttpStatus.NOT_FOUND, "Not found");
  }

  @ExceptionHandler(
      value = {IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
  protected ResponseEntity<Object> handleIllegalArguments(RuntimeException ex, WebRequest request) {
    return handle(ex, request, HttpStatus.BAD_REQUEST, "Illegal arguments");
  }

  @ExceptionHandler(
      value = {ForbiddenException.class})
  protected ResponseEntity<Object> handleForbidden(RuntimeException ex, WebRequest request) {
    return handle(ex, request, HttpStatus.FORBIDDEN, "Forbidden");
  }

  @ExceptionHandler(
      value = {DeletionNotPermittedException.class})
  protected ResponseEntity<Object> handleConflict(RuntimeException ex,
      WebRequest request) {
    return handle(ex, request, HttpStatus.CONFLICT, "Conflict");
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status,
      WebRequest request) {
    String name = ex.getParameterName();
    return handle(ex, request, HttpStatus.BAD_REQUEST, name + " parameter is missing");
  }

  private static Throwable findCause(Throwable throwable) {
    Objects.requireNonNull(throwable);
    Throwable rootCause = throwable;
    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
      rootCause = rootCause.getCause();
    }
    return rootCause;
  }

  private Model createErrorModel(Exception ex, WebRequest request, HttpStatus status,
      String fallbackMessage) {
    Literal title = ResourceFactory.createLangLiteral(status.toString(), "en");
    String message = findCause(ex).getMessage();
    Literal description = ResourceFactory
        .createLangLiteral(message == null || message.isBlank() ? fallbackMessage : message, "en");
    Resource error = ResourceFactory.createResource();
    return ModelFactory
        .createDefaultModel()
        .setNsPrefix("api", Api.getURI())
        .setNsPrefix("hydra", Hydra.getURI())
        .setNsPrefix("rdf", RDF.getURI())
        .setNsPrefix("rdfs", RDFS.getURI())
        .setNsPrefix("schema", SchemaOrg.getURI())
        .setNsPrefix("xsd", XSD.getURI()).setNsPrefix("core", Core.getURI())
        .add(error, RDF.type, Hydra.Status)
        .add(error, Hydra.statusCode, ResourceFactory.createTypedLiteral(status.value()))
        .add(error, Hydra.title, title)
        .add(error, Hydra.description, description);
  }

  private ResponseEntity<Object> handle(Exception ex, WebRequest request, HttpStatus status,
      String fallbackMessage) {
    Model error = createErrorModel(ex, request, status, fallbackMessage);
    Lang lang = new StringToLangConverter().convert(request.getHeader("accept"));
    return handleExceptionInternal(ex, serializer.serialize(error, lang), new HttpHeaders(), status,
        request);
  }
}
