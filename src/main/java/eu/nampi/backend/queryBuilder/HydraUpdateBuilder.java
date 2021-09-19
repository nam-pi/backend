package eu.nampi.backend.queryBuilder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import eu.nampi.backend.repository.HierarchyRepository;
import eu.nampi.backend.repository.TypeRepository;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.vocabulary.Core;

public class HydraUpdateBuilder extends AbstractHydraUpdateBuilder {

  public UUID id;

  public HydraUpdateBuilder(JenaService jenaService, HierarchyRepository hierarchyRepository,
      TypeRepository typeRepository, Lang lang, String baseUri, Resource mainType,
      List<Literal> labels, List<Literal> comments, List<Literal> texts,
      Optional<List<Resource>> optionalSameAs, UUID id) {
    super(jenaService, hierarchyRepository, typeRepository, baseUri, mainType);
    this.id = id;
    updateBuilder
        .addFilter(ef.sameTerm(VAR_MAIN, root))
        .addWhere(VAR_MAIN, VAR_PREDICATE, VAR_OBJECT)
        .addDelete(VAR_MAIN, VAR_PREDICATE, VAR_OBJECT)
        .addInsert(root, RDF.type, mainType);
    labels.forEach(label -> addInsert(root, RDFS.label, label));
    comments.forEach(labelcomment -> addInsert(root, RDFS.comment, labelcomment));
    texts.forEach(text -> addInsert(root, Core.hasText, text));
    optionalSameAs.ifPresent(
        sameAsList -> sameAsList.forEach(sameAs -> addInsert(root, Core.sameAs, sameAs)));
  }
}
