package rpc;

import java.io.IOException;

import rpc.netty.NettyRpcConnector;
import rpc.nio.NioRpcConnector;

public class RpcClient {
	
	private RpcProxy proxy ;
	private RpcConnector connector ;
	public RpcClient(String host,int port){
		proxy = new RpcProxy();
//		connector = new NioRpcConnector();
		connector = new NettyRpcConnector();
		connector.setHost(host);
		connector.setport(port);
		proxy.setConnector(connector);
		try {
			connector.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public <T> T refer(Class<T> clazz){
		return proxy.getProxy(clazz);
		
	}
	
}
