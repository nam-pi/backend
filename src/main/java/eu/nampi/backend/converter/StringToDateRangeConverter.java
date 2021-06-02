package eu.nampi.backend.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.convert.converter.Converter;
import eu.nampi.backend.model.DateRange;

public class StringToDateRangeConverter implements Converter<String, DateRange> {

  public static final Pattern SPLIT_REGEX = Pattern
      .compile("^(\\w{4})?(-(\\w{2})-(\\w{2}))?(-)?(\\w{4})?(-(\\w{2})-(\\w{2}))?$");

  public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private Optional<LocalDateTime> parse(String year, String month, String day) {
    Optional<LocalDateTime> date = Optional.empty();
    if (year != null) {
      String dateString = String.format("%s-%s-%s 00:00", year, month == null ? "01" : month,
          day == null ? "01" : day);
      date = Optional.of(LocalDateTime.parse(dateString, FORMATTER));
    }
    return date;
  }

  @Override
  public DateRange convert(String string) {
    Matcher matcher = SPLIT_REGEX.matcher(string);
    Optional<LocalDateTime> start = Optional.empty();
    Optional<LocalDateTime> end = Optional.empty();
    boolean isRange = false;
    while (matcher.find()) {
      isRange = Optional.ofNullable(matcher.group(5)).isPresent();
      String year1 = matcher.group(1);
      String month1 = matcher.group(3);
      String day1 = matcher.group(4);
      start = parse(year1, month1, day1);
      String year2 = matcher.group(6);
      String month2 = matcher.group(8);
      String day2 = matcher.group(9);
      end = parse(year2, month2, day2);
    }
    return new DateRange(start, end, isRange);
  }

}
