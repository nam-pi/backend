package eu.nampi.backend.repository;

import java.util.Optional;
import java.util.UUID;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.system.Txn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import eu.nampi.backend.ModelSerializer;
import eu.nampi.backend.domain.User;
import eu.nampi.backend.exception.InvalidUserDataException;
import eu.nampi.backend.exception.UserAlreadyExistingException;
import eu.nampi.backend.ontology.NampiCore;

@Repository
public class UserRepository {

    @Autowired
    RDFConnectionRemoteBuilder builder;

    public User create(User user) {
        if (user.getUserName().isEmpty() || user.getEmail().isEmpty()) {
            throw new InvalidUserDataException();
        }
        if (findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistingException();
        }
        user.setId(UUID.randomUUID());
        String query = "INSERT DATA {" + ModelSerializer.toNTriples(user.toModel()) + "}";
        try (RDFConnection conn = builder.build()) {
            Txn.executeWrite(conn, () -> {
                conn.update(query);
            });
        }
        return user;
    }

    public Optional<User> findByEmail(String email) {
        String query = "CONSTRUCT {?s ?p ?o} WHERE {?s <" + NampiCore.email.getURI() + "> '" + email + "' . ?s ?p ?o}";
        try (RDFConnection conn = builder.build()) {
            Model model = Txn.calculateRead(conn, () -> {
                return conn.queryConstruct(query);
            });
            if (!model.isEmpty()) {
                String userUri = model.listSubjectsWithProperty(NampiCore.userName).next().getURI();
                UUID id = UUID.fromString(userUri.substring(userUri.lastIndexOf("/") + 1));
                String userName = model.listObjectsOfProperty(NampiCore.userName).next().asLiteral().toString();
                return Optional.of(new User(id, userName, email));
            }
        }
        return Optional.empty();
    }

}