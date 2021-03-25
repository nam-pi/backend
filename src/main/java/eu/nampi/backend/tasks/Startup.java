package eu.nampi.backend.tasks;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.nampi.backend.service.JenaService;

@Component
public class Startup {

  @Autowired
  private JenaService jenaService;

  @PostConstruct
  public void init() {
    jenaService.initInfCache();
  }

}
