package eu.nampi.backend.configuration;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@KeycloakConfiguration
public class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

  @Bean
  public GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
    SimpleAuthorityMapper mapper = new SimpleAuthorityMapper();
    mapper.setConvertToUpperCase(true);
    return mapper;
  }

  @Bean
  @Primary
  KeycloakConfigResolver keycloakConfigResolver(KeycloakSpringBootProperties properties) {
    return new KeycloakPropertiesResolver(properties);
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    super.configure(http);
    http.addFilter(corsFilter().getFilter()).authorizeRequests()
        .antMatchers("/", "/doc", "/event/**", "/events/**", "/person/**", "/persons/**",
            "/aspect/**", "/aspects/**", "/author/**", "/authors/**", "/group/**", "/groups/**")
        .permitAll().antMatchers("/user/**").hasRole("USER").anyRequest().authenticated();
  }

  @Override
  protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
    KeycloakAuthenticationProvider keycloakAuthenticationProvider =
        keycloakAuthenticationProvider();
    auth.authenticationProvider(keycloakAuthenticationProvider);
  }

  @Override
  protected KeycloakAuthenticationProvider keycloakAuthenticationProvider() {
    final KeycloakAuthenticationProvider provider = super.keycloakAuthenticationProvider();
    provider.setGrantedAuthoritiesMapper(grantedAuthoritiesMapper());
    return provider;
  }

  @Override
  protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
    return new NullAuthenticatedSessionStrategy();
  }

  @Bean
  public FilterRegistrationBean<CorsFilter> corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
    config.addExposedHeader("Link");
    source.registerCorsConfiguration("/**", config);
    FilterRegistrationBean<CorsFilter> bean =
        new FilterRegistrationBean<CorsFilter>(new CorsFilter(source));
    bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return bean;
  }
}
