package co.otipc.smart.pathtrie;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

/**
 * Created by Chaoguo.Cui on 16/5/3.
 */
public class MyRestHandler implements RestHandler {

  private String path;

  public MyRestHandler(String path) {
    this.path = path;
  }

  @Override
  public void handleRequest(RestRequest request, RestChannel channel) throws Exception {
    System.out.println(" MyRestHandler  handleRequest ! " + path);
  }
}
