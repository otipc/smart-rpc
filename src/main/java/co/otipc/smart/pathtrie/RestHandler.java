package co.otipc.smart.pathtrie;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

/**
 * Created by Chaoguo.Cui on 16/5/3.
 */
public interface RestHandler {
  void handleRequest(RestRequest request, RestChannel channel) throws Exception;
}
