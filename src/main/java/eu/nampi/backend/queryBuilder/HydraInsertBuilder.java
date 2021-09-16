package eu.nampi.backend.queryBuilder;

import java.util.List;
import java.util.UUID;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import eu.nampi.backend.repository.HierarchyRepository;
import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.vocabulary.Core;

public class HydraInsertBuilder extends AbstractHydraUpdateBuilder {

  public UUID id;

  public HydraInsertBuilder(JenaService jenaService, HierarchyRepository hierarchyRepository,
      Lang lang, String baseUri, Resource mainType, List<Literal> labels,
      List<Literal> comments, List<Literal> texts, UUID id) {
    super(jenaService, hierarchyRepository, baseUri, mainType);
    this.id = id;
    updateBuilder.addInsert(root, RDF.type, mainType);
    labels.forEach(label -> updateBuilder.addInsert(root, RDFS.label, label));
    comments.forEach(labelcomment -> updateBuilder.addInsert(root, RDFS.comment, labelcomment));
    texts.forEach(text -> updateBuilder.addInsert(root, Core.hasText, text));
  }
}
