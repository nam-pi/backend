package eu.nampi.backend.repository;

import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_COMMENT;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_LABEL;
import static eu.nampi.backend.queryBuilder.AbstractHydraBuilder.VAR_MAIN;
import java.util.Optional;
import java.util.function.BiFunction;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.ExprFactory;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import eu.nampi.backend.queryBuilder.HydraBuilderFactory;
import eu.nampi.backend.queryBuilder.HydraSingleBuilder;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.util.UrlBuilder;
import eu.nampi.backend.vocabulary.Api;

@Repository
@CacheConfig(cacheNames = "hierarchies")
public class HierarchyRepository {

  @Autowired
  HydraBuilderFactory hydraBuilderFactory;

  @Autowired
  JenaService jenaService;

  @Autowired
  UrlBuilder urlBuilder;

  private static final Node VAR_CHILD = NodeFactory.createVariable("child");
  private static final Node VAR_CHILD_LABEL = NodeFactory.createVariable("childLabel");
  private static final Node VAR_CHILD_COMMENT = NodeFactory.createVariable("childComment");
  private static final Node VAR_PARENT = NodeFactory.createVariable("parent");
  private static final Node VAR_PARENT_LABEL = NodeFactory.createVariable("parentLabel");
  private static final Node VAR_PARENT_COMMENT = NodeFactory.createVariable("parentComment");

  @Cacheable(key = "{#parent, #child}")
  public boolean isSubnode(RDFNode parent, RDFNode child) {
    AskBuilder builder = new AskBuilder();
    ExprFactory ef = builder.getExprFactory();
    Node predicate = NodeFactory.createVariable("p");
    builder
        .addWhere(child, predicate, parent)
        .addFilter(ef.in(predicate, RDFS.subClassOf, RDFS.subPropertyOf));
    return jenaService.ask(builder);
  }

  @Cacheable(key = "{#lang, #iri}")
  public String findHierarchy(Lang lang, String iri) {
    HydraSingleBuilder builder = hydraBuilderFactory.singleBuilder(iri, RDFS.Resource, false);
    ExprFactory ef = builder.ef;
    Expr childNotRdf = ef.not(ef.strstarts(ef.str(VAR_CHILD), RDF.getURI()));
    Expr childNotRdfs = ef.not(ef.strstarts(ef.str(VAR_CHILD), RDFS.getURI()));
    Expr childNotOwl = ef.not(ef.strstarts(ef.str(VAR_CHILD), OWL.getURI()));
    Expr parentNotRdf = ef.not(ef.strstarts(ef.str(VAR_PARENT), RDF.getURI()));
    Expr parentNotRdfs = ef.not(ef.strstarts(ef.str(VAR_PARENT), RDFS.getURI()));
    Expr parentNotOwl = ef.not(ef.strstarts(ef.str(VAR_PARENT), OWL.getURI()));
    builder.coreData
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, RDFS.subClassOf, VAR_CHILD)
            .addFilter(childNotRdf)
            .addFilter(childNotRdfs)
            .addFilter(childNotOwl)
            .addWhere(VAR_CHILD, RDFS.subClassOf, VAR_PARENT)
            .addFilter(parentNotRdf)
            .addFilter(parentNotRdfs)
            .addFilter(parentNotOwl))
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, RDFS.subPropertyOf, VAR_CHILD)
            .addFilter(childNotRdf)
            .addFilter(childNotRdfs)
            .addFilter(childNotOwl)
            .addWhere(VAR_CHILD, RDFS.subPropertyOf, VAR_PARENT)
            .addFilter(parentNotRdf)
            .addFilter(parentNotRdfs)
            .addFilter(parentNotOwl))
        .addOptional(new WhereBuilder()
            .addWhere(VAR_MAIN, RDF.type, VAR_CHILD)
            .addFilter(childNotRdf)
            .addFilter(childNotRdfs)
            .addFilter(childNotOwl)
            .addWhere(VAR_CHILD, RDFS.subClassOf, VAR_PARENT)
            .addFilter(parentNotRdf)
            .addFilter(parentNotRdfs)
            .addFilter(parentNotOwl))
        .addOptional(VAR_CHILD, RDFS.label, VAR_CHILD_LABEL)
        .addOptional(VAR_CHILD, RDFS.comment, VAR_CHILD_COMMENT)
        .addOptional(VAR_PARENT, RDFS.label, VAR_PARENT_LABEL)
        .addOptional(VAR_PARENT, RDFS.comment, VAR_PARENT_COMMENT);
    Resource base = ResourceFactory.createResource(urlBuilder.endpointUri("hierarchy"));
    builder.model
        .add(base, RDF.type, Api.hierarchy)
        .add(base, Api.hierarchyRoot, builder.root);
    // Create the row mapper using the base and root (==main) resources
    BiFunction<Model, QuerySolution, RDFNode> rowMapper = (model, row) -> {
      Resource main = row.getResource(VAR_MAIN.toString());
      Resource child = row.getResource(VAR_CHILD.toString());
      Resource parent = row.getResource(VAR_PARENT.toString());
      model.add(main, RDF.type, RDFS.Resource);
      if (!main.equals(child)) {
        model.add(main, Api.descendantOf, child);
        model.add(child, RDF.type, RDFS.Resource);
      }
      if (!child.equals(parent)) {
        model.add(child, Api.descendantOf, parent);
        model.add(parent, RDF.type, RDFS.Resource);
      }
      Optional
          .ofNullable(row.getLiteral(VAR_LABEL.toString()))
          .ifPresent(literal -> model.add(main, RDFS.label, literal));
      Optional
          .ofNullable(row.getLiteral(VAR_COMMENT.toString()))
          .ifPresent(literal -> model.add(main, RDFS.comment, literal));
      Optional
          .ofNullable(row.getLiteral(VAR_CHILD_LABEL.toString()))
          .ifPresent(literal -> model.add(child, RDFS.label, literal));
      Optional
          .ofNullable(row.getLiteral(VAR_CHILD_COMMENT.toString()))
          .ifPresent(literal -> model.add(child, RDFS.comment, literal));
      Optional
          .ofNullable(row.getLiteral(VAR_PARENT_LABEL.toString()))
          .ifPresent(literal -> model.add(parent, RDFS.label, literal));
      Optional
          .ofNullable(row.getLiteral(VAR_PARENT_COMMENT.toString()))
          .ifPresent(literal -> model.add(parent, RDFS.comment, literal));
      return base;
    };
    return builder.query(rowMapper, lang);
  }
}
