package eu.nampi.backend;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.VCARD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ModelService {

    @Autowired
    private HttpServletRequest context;

    private static final Map<String, String> MOCK_DATA = createMockData();

    private static Map<String, String> createMockData() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("1", "Daniel Jeller");
        data.put("2", "Valerie Huemer");
        data.put("3", "Hans Olo");
        return data;
    }

    public Optional<Model> getModelByResourceUri(String id) {
        String data = MOCK_DATA.get(id);
        if (data == null) {
            return Optional.empty();
        } else {
            String uri = context.getRequestURL().toString();
            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(uri);
            resource.addLiteral(VCARD.FN, data);
            return Optional.of(model);
        }
    }

}