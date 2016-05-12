package rpc.test;
import java.io.IOException;

import rpc.RpcServer;

public class Server {

	public static void main(String[] args) throws IOException {
		RpcServer server = new RpcServer("127.0.0.1",1234);
		HelloServiceImpl impl = new HelloServiceImpl();
		server.export(HelloService.class, impl);

	}

}
