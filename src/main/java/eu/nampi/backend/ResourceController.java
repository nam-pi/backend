package eu.nampi.backend;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ResourceController {

    @Autowired
    private ModelService service;

    private String createResponse(String id, String language) {
        Optional<Model> optionalModel = service.getModelByResourceUri(id);
        if (optionalModel.isPresent()) {
            Model model = optionalModel.get();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            model.write(stream, language);
            try {
                return stream.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RequestException();
            }
        } else {
            throw new ResourceNotFoundException();
        }

    }

    @GetMapping(path = "/resource/{id}", produces = "application/ld+json;charset=UTF-8")
    public String resourceDefault(@PathVariable("id") String id) {
        return createResponse(id, "JSON-LD");
    }

    @GetMapping(path = "/resource/{id}", consumes = "application/rdf+xml", produces = "application/rdf+xml;charset=UTF-8")
    public String resourceRdfXml(@PathVariable("id") String id) {
        return createResponse(id, "RDF/XML");
    }

    @GetMapping(path = "/resource/{id}", consumes = "text/turtle", produces = "text/turtle;charset=UTF-8")
    public String resourceTextTurtle(@PathVariable("id") String id) {
        return createResponse(id, "TURTLE");
    }

}