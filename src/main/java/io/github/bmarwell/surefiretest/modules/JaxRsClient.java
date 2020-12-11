package io.github.bmarwell.surefiretest.modules;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JaxRsClient {
  private final URI baseuri;

  public JaxRsClient(URI baseuri) {
    this.baseuri = baseuri;
  }

  public Response executeQuery(String path, Map<String, String> queries) {
    WebTarget webTarget = ClientBuilder.newClient()
        .target(baseuri)
        .path(path);

    for (Map.Entry<String, String> entry : queries.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();
      webTarget = webTarget.queryParam(key, value);
    }

    return webTarget
        .request(MediaType.APPLICATION_JSON_TYPE)
        .buildGet()
        .invoke();
  }
}
