package eu.nampi.backend.service;

import java.util.function.Consumer;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import eu.nampi.backend.model.hydra.InterfaceHydraBuilder;

public interface JenaService {

  public Model construct(ConstructBuilder constructBuilder);

  public Model construct(InterfaceHydraBuilder hydraBuilder);

  public void initInfCache();

  public void select(SelectBuilder selectBuilder, Consumer<QuerySolution> rowAction);

  public void update(UpdateBuilder updateBuilder);
}
