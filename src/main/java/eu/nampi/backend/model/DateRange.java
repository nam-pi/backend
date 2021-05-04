package eu.nampi.backend.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@AllArgsConstructor
public class DateRange implements Serializable {

  private static final long serialVersionUID = -123852052656239217L;

  Optional<LocalDateTime> start;

  Optional<LocalDateTime> end;

  boolean isRange;

}
