package eu.nampi.backend.exception;

public class NotFoundException extends RuntimeException {

  public NotFoundException() {
    super();
  }

  public NotFoundException(String string) {
    super(string);
  }
}
