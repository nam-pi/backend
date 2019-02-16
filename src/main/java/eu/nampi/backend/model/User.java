package eu.nampi.backend.model;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import eu.nampi.backend.ontology.NampiCore;

public class User {

    private UUID id;

    @JsonProperty
    private String userName;

    @JsonProperty
    private String email;

    public User() {

    }

    public User(UUID id, String userName, String email) {
        this.id = id;
        this.userName = userName;
        this.email = email;
    }

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

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}