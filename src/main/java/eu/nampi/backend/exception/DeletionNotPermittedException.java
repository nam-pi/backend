package eu.nampi.backend.exception;

public class DeletionNotPermittedException extends RuntimeException {

  public DeletionNotPermittedException() {
    super();
  }

  public DeletionNotPermittedException(String string) {
    super(string);
  }
}
