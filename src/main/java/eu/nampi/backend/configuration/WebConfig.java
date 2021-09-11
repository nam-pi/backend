package eu.nampi.backend.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import eu.nampi.backend.converter.StringToDateRangeConverter;
import eu.nampi.backend.converter.StringToLangConverter;
import eu.nampi.backend.converter.StringToLiteralConverter;
import eu.nampi.backend.converter.StringToOrderByClausesConverter;
import eu.nampi.backend.converter.StringToResourceConverter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToDateRangeConverter());
    registry.addConverter(new StringToLangConverter());
    registry.addConverter(new StringToLiteralConverter());
    registry.addConverter(new StringToOrderByClausesConverter());
    registry.addConverter(new StringToResourceConverter());
  }
}
