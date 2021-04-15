package eu.nampi.backend.sparql;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;

import eu.nampi.backend.model.QueryParameters;
import eu.nampi.backend.vocabulary.Hydra;
import eu.nampi.backend.vocabulary.Vocab;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HydraCollectionBuilder extends AbstractHydraBuilder<HydraCollectionBuilder> {

  private Property orderByTemplateMappingProperty;

  private QueryParameters params;

  private List<String> templateVariables = new ArrayList<>();

  public HydraCollectionBuilder(QueryParameters params, Property mainType, Property orderByTemplateMappingProperty) {
    super(mainType);
    this.orderByTemplateMappingProperty = orderByTemplateMappingProperty;
    this.params = params;
    addSearchVariable("limit", Hydra.limit, false, params.getLimit());
    addSearchVariable("offset", Hydra.offset, false, params.getOffset());
    addSearchVariable("pageIndex", Hydra.pageIndex, false);
    addSearchVariable("orderBy", this.orderByTemplateMappingProperty, false,
        params.getOrderByClauses().empty() ? null : "'" + params.getOrderByClauses().toQueryString() + "'");
    addSearchVariable("type", Vocab.typeVar, false,
        params.getType().isPresent() ? "'" + URLEncoder.encode(params.getType().get(), Charset.defaultCharset()) + "'"
            : null);
    addSearchVariable("text", Vocab.textVar, false,
        params.getText().isPresent() ? "'" + params.getText().get() + "'" : null);
  }

  public HydraCollectionBuilder addSearchVariable(String name, Property property, boolean required) {
    return addSearchVariable(name, property, required, null);
  }

  public HydraCollectionBuilder addSearchVariable(String name, Property property, boolean required, Object value) {
    this.templateVariables.add(name);
    String templateMapping = mappingVar(name);
    // @formatter:off
    this.builder
      .addConstruct(templateMapping, RDF.type, Hydra.IriTemplateMapping)
      .addConstruct(templateMapping, Hydra.property, property)
      .addConstruct(templateMapping, Hydra.required, required ? "true" : "false")
      .addConstruct(templateMapping, Hydra.variable, name)
      .addConstruct("?search", Hydra.mapping, templateMapping);
    // @formatter:on
    if (value != null) {
      this.builder.addBind(this.builder.makeExpr(String.valueOf(value)), pad(name));
    }
    return getThis();
  }

  @Override
  public Query build() {
    params.getType().ifPresent(t -> {
      String iri = "<" + t + ">";
      this.mainWhere.addWhere(MAIN_SUBJ, RDF.type, iri);
      this.builder.addConstruct(MAIN_SUBJ, RDF.type, iri);
    });
    params.getText().ifPresent(t -> {
      this.mainWhere.addFilter(ef.regex(MAIN_LABEL, t, "i"));
    });
    try {
      // @formatter:off
      SelectBuilder countSelect = new SelectBuilder()
        .addVar("COUNT(*)", "?count")
        .addWhere(this.mainWhere);
      SelectBuilder dataSelect = new SelectBuilder()
        .addVar("*")
        .addWhere(this.mainWhere);
      params
        .getOrderByClauses()
        .appendAllTo(dataSelect);
      dataSelect.addOrderBy(MAIN_SUBJ)
        .setLimit(this.params.getLimit())
        .setOffset(this.params.getOffset());
      SelectBuilder searchSelect = new SelectBuilder()
        .addVar("*")
        .addBind("bnode()", "?search")
        .addBind(this.ef.concat(this.params.getBaseUrl(), "{?" + this.templateVariables.stream().collect(Collectors.joining(",")) + "}"), "?template")
        .addBind("bnode()", "?manages");
      for (String string : templateVariables) {
        searchSelect.addBind("bnode()", mappingVar(string));
      }
      this.builder
        .addWhere(new WhereBuilder()
          .addUnion(countSelect)
          .addUnion(dataSelect)
          .addUnion(searchSelect))
        .addConstruct("?col", Hydra.member, MAIN_SUBJ)
        .addConstruct("?col", Hydra.search, "?search")
        .addConstruct("?col", Hydra.totalItems, "?count")
        .addConstruct("?col", Hydra.view, "?view")
        .addConstruct("?col", RDF.type, Hydra.Collection)
        .addConstruct("?col", Hydra.manages, "?manages")
        .addConstruct("?manages", Hydra.object, mainType)
        .addConstruct("?search", Hydra.template, "?template")
        .addConstruct("?search", Hydra.variableRepresentation, Hydra.BasicRepresentation)
        .addConstruct("?search", RDF.type, Hydra.IriTemplate)
        .addConstruct("?view", Hydra.first, "?first")
        .addConstruct("?view", Hydra.last, "?last")
        .addConstruct("?view", Hydra.next, "?next")
        .addConstruct("?view", Hydra.previous, "?prev")
        .addConstruct("?view", RDF.type, Hydra.PartialCollectionView)
        .addBind(ef.asExpr(this.params.getBaseUrl()), "?baseUrl")
        .addBind(ef.asExpr(this.params.getRelativePath()), "?path")
        .addBind(ef.asExpr(this.params.isCustomLimit()), "?customMeta")
        .addBind(ef.iri("?baseUrl"), "?col")
        .addBind("concat(?baseUrl, " + IntStream.range(0, templateVariables.size()).mapToObj(idx -> {
          String val = templateVariables.get(idx);
          return "if(bound(?" + val + "), concat('" + (idx == 0 ? "?" : "&") + val + "=', xsd:string(" + pad(val) + ")), '')";
        }).collect(Collectors.joining(", ")) + ")", "?viewUri")
        .addBind("iri(?viewUri)", "?view")
        .addBind("iri(replace(?viewUri, 'offset=\\\\d*', 'offset=0'))", "?first")
        .addBind("if(?count > ?limit, iri(replace(?viewUri, 'offset=\\\\d*', concat('offset=', if(?count = 0, xsd:string(0), xsd:string(?count - ?limit))))), 1+'')", "?last")
        .addBind("?offset - ?limit", "?prevOffset")
        .addBind("if(?prevOffset >= 0, iri(replace(?viewUri, 'offset=\\\\d*', concat('offset=', xsd:string(?prevOffset)))), 1+'')", "?prev")
        .addBind("?offset + ?limit", "?nextOffset")
        .addBind("if(?nextOffset < ?count, iri(replace(?viewUri, 'offset=\\\\d*', concat('offset=', xsd:string(?nextOffset)))), 1+'')", "?next");
      // @formatter:on
      return this.builder.build();
    } catch (ParseException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  @Override
  protected HydraCollectionBuilder getThis() {
    return this;
  }

  private String mappingVar(String var) {
    return pad(var) + "Mapping";
  }

}
