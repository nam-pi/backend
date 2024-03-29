package eu.nampi.backend.configuration;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

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
    http
        .cors()
        .and()
        .authorizeRequests()
        .antMatchers(HttpMethod.POST, "/**").hasRole("AUTHOR")
        .antMatchers(HttpMethod.PUT, "/**").hasRole("AUTHOR")
        .antMatchers(HttpMethod.DELETE, "/**").hasRole("AUTHOR")
        .antMatchers(HttpMethod.GET, "/users/**").hasRole("USER")
        .antMatchers(HttpMethod.GET, "/**").permitAll()
        .anyRequest().authenticated();
    http.csrf().disable(); // Todo implement correct CSRF handling
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
}
