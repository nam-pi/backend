package eu.nampi.backend.repository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;

import com.github.jsonldjava.core.JsonLdOptions;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.JsonLDWriteContext;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eu.nampi.backend.service.JenaService;
import eu.nampi.backend.sparql.InterfaceHydraBuilder;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
public abstract class AbstractHydraRepository {

  @Autowired
  private JenaService jenaService;

  protected Model construct(InterfaceHydraBuilder builder) {
    return jenaService.construct(builder);
  }

  protected String createFrame(Model model, Resource startId) {
    return "{\"@context\": " + extractContext(model) + ", \"@id\": \"" + startId.toString() + "\"}";
  }

  protected String endpointUri() {
    return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
  }

  protected String endpointUri(String... path) {
    StringBuilder builder = new StringBuilder(endpointUri());
    for (String string : path) {
      builder.append("/").append(string);
    }
    return builder.toString();
  }

  protected String extractContext(Model model) {
    Configuration conf = Configuration.builder().jsonProvider(new JacksonJsonProvider()).build();

    StringWriter writer = new StringWriter();
    RDFDataMgr.write(writer, model, RDFFormat.JSONLD);
    String serialized = writer.toString().replace("@context", "context");

    Map<String, String> res = JsonPath.using(conf).parse(serialized).read("$.context");
    return new JSONObject(res).toJSONString();
  }

  protected String individualsUri(Resource type) {
    return endpointUri(type.getLocalName());
  }

  protected String individualsUri(Resource type, UUID id) {
    return individualsUri(type) + "/" + id;
  }

  protected String newIndividualUri(Resource type) {
    return individualsUri(type, UUID.randomUUID());
  }

  protected String serialize(Model model, Lang lang) {
    StringWriter writer = new StringWriter();
    RDFDataMgr.write(writer, model, lang);
    return writer.toString();
  }

  protected String serialize(Model model, Lang lang, Resource startId) {
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      if (lang == Lang.JSONLD) {
        JsonLDWriteContext ctx = new JsonLDWriteContext();
        JsonLdOptions options = new JsonLdOptions();
        options.setOmitGraph(true);
        ctx.setFrame(createFrame(model, startId));
        ctx.setOptions(options);
        RDFWriter w = RDFWriter.create().format(RDFFormat.JSONLD_FRAME_FLAT).context(ctx).source(model).build();
        w.output(out);
      } else {
        RDFDataMgr.write(out, model, lang);
      }
      return out.toString("UTF-8");
    } catch (IOException e) {
      log.error(e.getMessage());
      return "";
    }
  }
}
