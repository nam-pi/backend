package eu.nampi.backend.service;

import java.util.function.Consumer;
import org.apache.jena.arq.querybuilder.AskBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.UpdateBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.query.QuerySolution;

public interface JenaService {

  public boolean ask(AskBuilder askBuilder);

  public int count(WhereBuilder whereBuilder, Node distinctVariable);

  public void initInfCache();

  public void select(SelectBuilder selectBuilder, Consumer<QuerySolution> rowAction);

  public void update(UpdateBuilder updateBuilder);
}
