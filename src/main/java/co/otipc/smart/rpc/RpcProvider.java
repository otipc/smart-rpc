package co.otipc.smart.rpc;


public class RpcProvider {

  public static void main(String[] args) throws Exception {

    HelloService service = new HelloServiceImpl();

    Framework.export(service, 1234);

  }

}
