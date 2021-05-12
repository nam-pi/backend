package eu.nampi.backend.model.hydra;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import eu.nampi.backend.vocabulary.Hydra;

public class ParameterMapper {
  private final String baseUrl;
  private final ConstructBuilder construct;
  private final List<String> parts = new ArrayList<>();
  private final Node varSearch;
  private final SelectBuilder bind;
  List<String> viewParts = new ArrayList<>();

  public ParameterMapper(String baseUrl, Node varSearch, ConstructBuilder construct, SelectBuilder bind) {
    this.baseUrl = baseUrl;
    this.construct = construct;
    this.bind = bind;
    this.varSearch = varSearch;
  }

  public ParameterMapper add(String variable, Resource type, Object value) {
    return this.add(variable, type, value, false);
  }

  public ParameterMapper add(String variable, Resource type, Object value, boolean required) {
    this.parts.add(variable);
    Node bnode = NodeFactory.createVariable("mapping_" + variable);
    this.bind.addBind(construct.getExprFactory().bnode(), bnode);
    // @formatter:off
    this.construct
      .addConstruct(varSearch, Hydra.mapping, bnode)
      .addConstruct(bnode, RDF.type, Hydra.IriTemplateMapping)
      .addConstruct(bnode, Hydra.variable, variable)
      .addConstruct(bnode, Hydra.property, type)
      .addConstruct(bnode, Hydra.required, required);
    // @formatter:on

    if (value != null && !value.toString().equals("")) {
      viewParts.add(variable + "=" + URLEncoder.encode(value.toString(), Charset.defaultCharset()));
    }
    return this;
  }

  public Node addTemplate(Node collection) {
    Node bnode = NodeFactory.createVariable("template");
    StringBuilder templateBuilder = new StringBuilder(this.baseUrl);
    // @formatter:off
    if (!parts.isEmpty()) {
      templateBuilder
        .append(parts.stream().sorted().collect(Collectors.joining(",", "{?", "}")));
    }
    StringBuilder viewStringBuilder = new StringBuilder(this.baseUrl);
    if (!viewParts.isEmpty()) {
      viewStringBuilder
        .append(viewParts.stream().sorted().collect(Collectors.joining("&", "?", "")));
    }
    Node resView = NodeFactory.createURI(viewStringBuilder.toString());
    this.construct
      .addConstruct(collection, Hydra.view, resView)
      .addConstruct(resView, RDF.type, Hydra.PartialCollectionView);
    this.bind
      .addBind(this.construct.getExprFactory().asExpr(templateBuilder.toString()), bnode);
    this.construct
      .addConstruct(varSearch, Hydra.template, bnode);
    // @formatter:on
    return resView;
  }

}
