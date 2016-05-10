package co.otipc.smart.pathtrie;



/**
 * Created by Chaoguo.Cui on 16/5/3.
 */
public class TestApp {

  public final PathTrie<RestHandler> getHandlers = new PathTrie<>(RestUtils.REST_DECODER);

  public static void main(String[] args) throws Exception {
    TestApp app = new TestApp();

    app.getHandlers.insert("/test", new MyRestHandler("/test"));
    app.getHandlers.insert("/test/{id}", new MyRestHandler("/test/{id}"));
    app.getHandlers.insert("/test/{id}/_search", new MyRestHandler("/test/{id}/_search"));

    RestHandler handler = app.getHandlers.retrieve("/test");
    RestHandler handler2 = app.getHandlers.retrieve("/test/3");
    RestHandler handler3 = app.getHandlers.retrieve("/test/3/_search");

    handler.handleRequest(null, null);
    handler2.handleRequest(null, null);
    handler3.handleRequest(null, null);


  }





}
