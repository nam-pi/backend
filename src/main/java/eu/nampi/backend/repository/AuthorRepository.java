package eu.nampi.backend.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import eu.nampi.backend.model.Author;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.vocabulary.Core;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AuthorRepository extends AbstractHydraRepository {

  @Autowired
  private JenaService jenaService;

  public Optional<Author> findOne(UUID rdfId) {
    AtomicReference<Optional<Author>> authorRef = new AtomicReference<>(Optional.empty());
    SelectBuilder selectBuilder = new SelectBuilder();
    String iri = individualsUri(Core.author, rdfId);
    try {
      selectBuilder.addValueVar("?l").addWhere("?a", RDF.type, Core.author)
          .addWhere("?a", RDFS.label, "?l").addFilter("?a = <" + iri + ">");
    } catch (ParseException e) {
      log.warn(e.getMessage());
    }
    jenaService.select(selectBuilder, (qs) -> {
      String label = qs.getLiteral("?l").getString();
      authorRef.set(Optional.of(new Author(iri, rdfId, label)));
    });
    return authorRef.get();
  }

  public Author addOne(UUID rdfId, String label) {
    String iri = individualsUri(Core.author, rdfId);
    Resource authorRes = ResourceFactory.createResource(iri);
    UpdateBuilder updateBuilder = new UpdateBuilder().addInsert(authorRes, RDF.type, Core.author)
        .addInsert(authorRes, RDFS.label, label);
    jenaService.update(updateBuilder);
    return new Author(iri, rdfId, label);
  }

  public Author updateLabel(Author author, String newLabel) {
    UpdateBuilder updateBuilder = new UpdateBuilder();
    try {
      updateBuilder.addDelete("?a", RDFS.label, "?l").addInsert("?a", RDFS.label, newLabel)
          .addWhere(new WhereBuilder().addWhere("?a", RDF.type, Core.author)
              .addWhere("?a", RDFS.label, "?l").addFilter("?a = <" + author.getIri() + ">"));
    } catch (ParseException e) {
      log.error(e.getMessage());
    }
    author.setLabel(newLabel);
    jenaService.update(updateBuilder);
    return author;
  }

}
