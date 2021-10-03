package eu.nampi.backend.exception;

public class ForbiddenException extends RuntimeException {

  public ForbiddenException() {
    super();
  }

  public ForbiddenException(String string) {
    super(string);
  }
}
