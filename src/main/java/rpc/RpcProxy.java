package rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpcProxy {
	private RpcConnector connector;
	
public RpcConnector getConnector() {
		return connector;
	}

	public void setConnector(RpcConnector connector) {
		this.connector = connector;
	}

@SuppressWarnings("unchecked")
public  <T> T getProxy(final Class<T> clazz) {
		
		InvocationHandler handler = new InvocationHandler() {
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				RpcRequst requst = new RpcRequst();
				requst.setClassName(clazz.getName());
				requst.setMethodName(method.getName());
				requst.setParameterTypes(method.getParameterTypes());
				requst.setParameters(args);
				RpcResponse response=connector.invoke(requst);
				return response.getResult();
			}
		};
		T t = (T) Proxy.newProxyInstance(RpcProxy.class.getClassLoader(), new Class[] {clazz}, handler);
		return t;
	}
}
