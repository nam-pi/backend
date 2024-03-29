package eu.nampi.backend.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import com.github.jsonldjava.core.JsonLdOptions;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.JsonLDWriteContext;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.springframework.stereotype.Component;
import eu.nampi.backend.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

@Slf4j
@Component
public class Serializer {

  public String serialize(Model model, Lang lang) {
    StringWriter writer = new StringWriter();
    RDFDataMgr.write(writer, model, lang);
    return writer.toString();
  }

  public String serialize(Model model, Lang lang, Resource startId) {
    if (!model.contains(startId, null, (RDFNode) null)) {
      throw new NotFoundException();
    }
    try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      if (lang == Lang.JSONLD) {
        JsonLDWriteContext ctx = new JsonLDWriteContext();
        JsonLdOptions options = new JsonLdOptions();
        options.setOmitGraph(true);
        ctx.setFrame(createFrame(model, startId));
        ctx.setOptions(options);
        RDFWriter w = RDFWriter.create().format(RDFFormat.JSONLD_FRAME_FLAT).context(ctx)
            .source(model).build();
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

  private String createFrame(Model model, Resource startId) {
    return "{\"@context\": " + extractContext(model) + ", \"@id\": \"" + startId.toString() + "\"}";
  }

  private String extractContext(Model model) {
    Configuration conf = Configuration.builder().jsonProvider(new JacksonJsonProvider()).build();
    StringWriter writer = new StringWriter();
    RDFDataMgr.write(writer, model, RDFFormat.JSONLD);
    String serialized = writer.toString().replace("@context", "context");
    Map<String, String> res = JsonPath.using(conf).parse(serialized).read("$.context");
    return new JSONObject(res).toJSONString();
  }
}
