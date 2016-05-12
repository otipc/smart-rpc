package rpc;

import rpc.nio.NioRpcConnector;

public class RpcClient {
	
	private RpcProxy proxy ;
	private RpcConnector connector ;
	public RpcClient(String host,int port){
		proxy = new RpcProxy();
		connector = new NioRpcConnector();
		connector.setHost(host);
		connector.setport(port);
		connector.start();
		proxy.setConnector(connector);
	}
	
	public <T> T refer(Class<T> clazz){
		return proxy.getProxy(clazz);
		
	}
	
}
