package eu.nampi.backend.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.nampi.backend.repository.OwlRepository;

@Component
public class Startup {

  @Autowired
  private OwlRepository owlRepository;

  @Value("${nampi.core-owl-url}")
  private String coreOwlUrl;

  @Value("${nampi.other-owl-urls}")
  private List<String> otherOwlUrls;

  @PostConstruct
  public void init() {
    owlRepository.deleteAll();
    List<String> owls = new ArrayList<>();
    owls.add(coreOwlUrl);
    owls.addAll(otherOwlUrls);
    owlRepository.storeOwls(owls);
  }

}
