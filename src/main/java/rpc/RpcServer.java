package rpc;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import rpc.netty.NettyRpcAcceptor;
import rpc.nio.NioRpcAcceptor;

public class RpcServer {
	protected ConcurrentHashMap<String, Object> serviceEngine = new ConcurrentHashMap<>();
	private RpcProcessor processor;
	private RpcAcceptor acceptor;
	public RpcServer(){}
	public RpcServer(String host,int port){
//		acceptor = new NioRpcAcceptor();
		acceptor = new NettyRpcAcceptor();
		acceptor.setHost(host);
		acceptor.setport(port);		
		processor = new RpcProcessor();
		processor.setServer(this);
		acceptor.setProcessor(processor);
		
		try {
			acceptor.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void export(Class<?> clazz, Object obj) {
		export(clazz, obj, null);
	}

	public void export(Class<?> clazz, Object obj, String version) {

		try {
			obj.getClass().asSubclass(clazz);
		} catch (ClassCastException e) {
			throw new RpcException(obj.getClass().getName() + " can't cast " + clazz.getName());
		}
		if (version == null) {
			version = Constants.DEFAULT_VERSION;
		}
		String exekey = this.genExeKey(clazz.getName(), version);
		Object service = serviceEngine.get(exekey);
		if (service != null && service != obj) {
			throw new RpcException("can't register service " + clazz.getName() + " again");
		}
		if (obj == service || obj == null) {
			return;
		}
		serviceEngine.put(exekey, obj);
	}

	private String genExeKey(String service, String version) {
		if (version != null) {
			return service + "_" + version;
		}
		return service;
	}

	public Object findService(String clazzName) {
		return findService(clazzName, null);
	}

	public Object findService(String clazzName, String version) {

		if (version == null) {
			version = Constants.DEFAULT_VERSION;
		}
		String exekey = this.genExeKey(clazzName, version);
		Object service = serviceEngine.get(exekey);
		return service;

	}
}
