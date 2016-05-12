package rpc.test;

import rpc.RpcClient;

public class Clinet {

	public static void main(String[] args) throws Exception {
		RpcClient client = new RpcClient("127.0.0.1",1234);
		Thread.sleep(1000);
		HelloService service = client.refer(HelloService.class);
		System.out.println(service.hello("test rp0000c"));

	}

}
