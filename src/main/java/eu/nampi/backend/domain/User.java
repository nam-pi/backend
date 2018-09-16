package eu.nampi.backend.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import eu.nampi.backend.ontology.NampiCore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;

    @JsonProperty
    private String userName;

    @JsonProperty
    private String email;

    public Model toModel() {
        return toResource().getModel();
    }

    public Resource toResource() {
        Model model = ModelFactory.createDefaultModel();
        Resource user = model.createResource(NampiCore.BASE_URI + "users/" + id, NampiCore.User);
        user.addProperty(NampiCore.userName, userName);
        user.addProperty(NampiCore.email, email);
        return user;
    }

}