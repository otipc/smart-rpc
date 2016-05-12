package rpc;

public class RpcClient {
	
	private RpcProxy proxy = new RpcProxy();
	
	public <T> T refer(Class<T> clazz){
		return proxy.getProxy(clazz);
		
	}
	
}
