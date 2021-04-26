package eu.nampi.backend.model.hydra;

import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.FrontsTriple;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import eu.nampi.backend.vocabulary.Core;
import eu.nampi.backend.vocabulary.Doc;
import eu.nampi.backend.vocabulary.Hydra;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractHydraBuilder<T extends AbstractHydraBuilder<T>>
    implements InterfaceHydraBuilder {


  protected abstract T getThis();

  public static final String MAIN_LABEL = "?label";

  public static final String MAIN_SUBJ = "?main";

  protected ConstructBuilder builder = new ConstructBuilder();

  protected ExprFactory ef;

  protected WhereBuilder mainWhere = new WhereBuilder();

  protected Property mainType;

  public AbstractHydraBuilder(Property mainType) {
    this.mainType = mainType;
    this.mainWhere.addPrefix("xsd", XSD.getURI()).addPrefix("rdfs", RDFS.getURI())
        .addPrefix("core", Core.getURI()).addWhere(MAIN_SUBJ, RDF.type, this.mainType)
        .addWhere(MAIN_SUBJ, RDFS.label, MAIN_LABEL);
    this.builder.addPrefix("doc", Doc.getURI()).addPrefix("core", Core.getURI())
        .addPrefix("hydra", Hydra.getURI()).addPrefix("rdf", RDF.getURI())
        .addPrefix("rdfs", RDFS.getURI()).addPrefix("xsd", XSD.getURI())
        .addConstruct(MAIN_SUBJ, RDF.type, this.mainType)
        .addConstruct(MAIN_SUBJ, RDFS.label, MAIN_LABEL);
    this.ef = this.builder.getExprFactory();
  }


  public T addBind(String expression, Object var) {
    this.mainWhere.addBind(this.mainWhere.makeExpr(expression), var);
    return getThis();
  }

  public T addConstruct(FrontsTriple t) {
    this.builder.addConstruct(t);
    return getThis();
  }

  public T addConstruct(Object s, Object p, Object o) {
    this.builder.addConstruct(s, p, o);
    return getThis();
  }

  public T addConstruct(Triple t) {
    this.builder.addConstruct(t);
    return getThis();
  }

  public T addFilter(String filter) {
    try {
      this.mainWhere.addFilter(filter);
    } catch (ParseException e) {
      log.error(e.getMessage());
    }
    return getThis();
  }

  public T addMainConstruct(Object p, Object o) {
    return addConstruct(MAIN_SUBJ, p, o);
  }

  public T addMainWhere(Object p, Object o) {
    return addWhere(MAIN_SUBJ, p, o);
  }

  public T addOptional(WhereBuilder whereClause) {
    this.mainWhere.addOptional(whereClause);
    return getThis();
  }

  public T addOptional(Object s, Object p, Object o) {
    this.mainWhere.addOptional(s, p, o);
    return getThis();
  }

  public T addMainOptional(Object p, Object o) {
    this.mainWhere.addOptional(MAIN_SUBJ, p, o);
    return getThis();
  }

  public T addUnions(WhereBuilder... builder) {
    WhereBuilder nw = new WhereBuilder();
    for (WhereBuilder whereBuilder : builder) {
      nw.addUnion(whereBuilder);
    }
    this.mainWhere.addWhere(nw);
    return getThis();
  }

  public T addValues(Object var, Object... values) {
    this.mainWhere.addWhereValueVar(var, values);
    return getThis();
  }

  public T addWhere(Object s, Object p, Object o) {
    this.mainWhere.addWhere(s, p, o);
    return getThis();
  }

  public T addWhere(WhereBuilder whereClause) {
    this.mainWhere.addWhere(whereClause);
    return getThis();
  }

  @Override
  public String buildString() {
    return build().toString();
  }

  protected String pad(String var) {
    return var.startsWith("?") ? var : "?" + var;
  }
}
