package rpc;

interface RpcProvider {
	public void regester(Class<?> clazz,Object obj);
	public void regester(Class<?> clazz,Class<?> implclass);
}
