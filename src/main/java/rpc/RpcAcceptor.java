package rpc;

import java.io.IOException;

public interface RpcAcceptor {
	public void write(RpcResponse response) throws IOException;
	public void setHost(String host);
	public void setport(int port);
	public void start() throws IOException;
	public void stop() throws IOException;
	public void setProcessor(RpcProcessor processor);
}
