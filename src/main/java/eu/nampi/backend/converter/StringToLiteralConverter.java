package eu.nampi.backend.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.core.convert.converter.Converter;

public class StringToLiteralConverter implements Converter<String, Literal> {

  private final static Pattern fullLangPattern = Pattern.compile(
      "^(?<text>.+)@(?<tag>((?<grandfathered>(en-GB-oed|i-ami|i-bnn|i-default|i-enochian|i-hak|i-klingon|i-lux|i-mingo|i-navajo|i-pwn|i-tao|i-tay|i-tsu|sgn-BE-FR|sgn-BE-NL|sgn-CH-DE)|(art-lojban|cel-gaulish|no-bok|no-nyn|zh-guoyu|zh-hakka|zh-min|zh-min-nan|zh-xiang))|((?<language>([A-Za-z]{2,3}(-(?<extlang>[A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-(?<script>[A-Za-z]{4}))?(-(?<region>[A-Za-z]{2}|[0-9]{3}))?(-(?<variant>[A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-(?<extension>[0-9A-WY-Za-wy-z](-[A-Za-z0-9]{2,8})+))*(-(?<privateUse>x(-[A-Za-z0-9]{1,8})+))?)|(?<privateUse1>x(-[A-Za-z0-9]{1,8})+)))?$");

  private final static Pattern simpleLangPattern = Pattern.compile(
      "^(?<text>.+)@(?<tag>[A-Za-z]{2,4}([_-][A-Za-z]{4})?([_-]([A-Za-z]{2}|[0-9]{3}))?)$");

  @Override
  public Literal convert(String string) {
    // Only tries to match languages if the string contains an "@"
    if (string.contains("@")) {
      // Tries a faster simple pattern
      Matcher simpleMatcher = simpleLangPattern.matcher(string);
      if (simpleMatcher.matches()) {
        String text = simpleMatcher.group("text");
        String lang = simpleMatcher.group("tag");
        if (text != null && lang != null && !lang.isBlank()) {
          return ResourceFactory.createLangLiteral(text, lang);
        }
      }
      // Tries the full pattern
      Matcher fullMatcher = fullLangPattern.matcher(string);
      if (fullMatcher.matches()) {
        String text = fullMatcher.group("text");
        String lang = fullMatcher.group("tag");
        if (text != null && lang != null && !lang.isBlank()) {
          return ResourceFactory.createLangLiteral(text, lang);
        }
      }
    }
    // Returns the original string if no previous pattern was found
    return ResourceFactory.createStringLiteral(string);
  }
}
