package io.github.bmarwell.surefiretest.modules;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.okForJson;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.lanwen.wiremock.ext.WiremockResolver;
import ru.lanwen.wiremock.ext.WiremockUriResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.core.Response;

@ExtendWith( {
    WiremockResolver.class,
    WiremockUriResolver.class
})
public class JaxRsClientTest {


  private static final String BASEPATH = "/api";
  private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;
  private static final ObjectMapper OBJECT_MAPPER = JacksonObjectMapperProvider.getInstance();

  private final WireMockServer server;

  private JaxRsClient client;

  public JaxRsClientTest(@WiremockResolver.Wiremock final WireMockServer server, @WiremockUriResolver.WiremockUri final String uri) {
    this.server = server;
  }

  @BeforeEach
  public void setUp() {
    final String uri = this.server.url(BASEPATH);
    this.client = new JaxRsClient(URI.create(uri));
  }

  @Test
  public void testEvaluateQuery_instances() throws IOException {
    final JsonNode jsonNode = OBJECT_MAPPER.readTree(
        "{\n"
            + "      \"id\": \"264be9226fd8e3c027e0cc60cb1f4e44\",\n"
            + "      \"author\": \"me\",\n"
            + "      \"title\": \"how to create a surefire test\"\n"
            + "    }");

    this.server.stubFor(
        any(urlPathEqualTo(BASEPATH + "/book"))
            .withQueryParam("author", WireMock.matching(".*"))
            .willReturn(
                okForJson(jsonNode)
                    .withHeader("X-Total-Count", "" + 1)
            )
            .withName("testEvaluateQuery")
    );

    Map<String,String> queries = new ConcurrentHashMap<>();
    queries.put("author", "me");
    Response response = this.client.executeQuery("book", queries);

    final Long totalCount = Long.parseLong(response.getHeaderString("X-Total-Count"));

    final InputStream entity = (InputStream) response.getEntity();
    final Map<String, String> map = OBJECT_MAPPER.readValue(entity, Map.class);

    assertThat("one instance should have been returned.", totalCount, is(1L));
    assertThat("author should be well-known.", map.get("author"), is("me"));
    assertThat("id should be well-known.", map.get("id"), is("264be9226fd8e3c027e0cc60cb1f4e44"));
  }
}
