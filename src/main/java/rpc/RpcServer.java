package rpc;

import java.util.concurrent.ConcurrentHashMap;

public class RpcServer   {
	protected ConcurrentHashMap<String, RpcServiceBean> exeCache = new ConcurrentHashMap<String, RpcServiceBean>();

	public void export(Class<?> clazz, Object obj) {
		export(clazz, obj, null);
	}

	public void export(Class<?> clazz, Object obj, String version) {

		try {
			obj.getClass().asSubclass(clazz);
		} catch (ClassCastException e) {
			throw new RpcException(obj.getClass().getName() +" can't cast " + clazz.getName());
		}
		if (version == null) {
			version = Constants.DEFAULT_VERSION;
		}
		String exekey = this.genExeKey(clazz.getName(), version);
		Object service = exeCache.get(exekey);
		if (service != null && service != obj) {
			throw new RpcException("can't register service " + clazz.getName() + " again");
		}
		if (obj == service || obj == null) {
			return;
		}
		exeCache.put(exekey, new RpcServiceBean(clazz, obj, version));
	}

	private String genExeKey(String service, String version) {
		if (version != null) {
			return service + "_" + version;
		}
		return service;
	}
	public Object findService(String clazzName){
		return findService(clazzName,null);
	}
public Object findService(String clazzName,String version){
	
	if (version == null) {
		version = Constants.DEFAULT_VERSION;
	}
	String exekey = this.genExeKey(clazzName, version);
	Object service = exeCache.get(exekey);
	return service;
		
	}
}
