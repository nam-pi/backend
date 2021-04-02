package eu.nampi.backend.sparql;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import eu.nampi.backend.vocabulary.Api;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Hydra;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HydraSingleBuilder implements InterfaceHydraBuilder {

  public static final String MAIN_LABEL = "?label";

  public static final String MAIN_SUBJ = "?main";

  private ConstructBuilder builder = new ConstructBuilder();

  private WhereBuilder mainWhere = new WhereBuilder();

  public HydraSingleBuilder(String uri, Property mainType) {
    this.builder.addPrefix("api", Api.getURI()).addPrefix("core", Core.getURI()).addPrefix("hydra", Hydra.getURI())
        .addPrefix("rdf", RDF.getURI()).addPrefix("rdfs", RDFS.getURI()).addPrefix("xsd", XSD.getURI())
        .addConstruct(MAIN_SUBJ, RDF.type, mainType).addConstruct(MAIN_SUBJ, RDFS.label, MAIN_LABEL);
    try {
      this.mainWhere.addWhere(MAIN_SUBJ, RDF.type, mainType).addWhere(MAIN_SUBJ, RDFS.label, MAIN_LABEL)
          .addFilter(MAIN_SUBJ + " = <" + uri + ">");
    } catch (ParseException e) {
      log.error(e.getMessage());
    }
  }

  public HydraSingleBuilder addData(Object s, Object p, Object o) {
    this.builder.addConstruct(s, p, o);
    this.mainWhere.addWhere(s, p, o);
    return this;
  }

  public HydraSingleBuilder addMainData(Object p, Object o) {
    return this.addData(MAIN_SUBJ, p, o);
  }

  @Override
  public Query build() {
    return this.builder.addWhere(this.mainWhere).build();
  }

  @Override
  public String buildString() {
    return build().toString();
  }

}
