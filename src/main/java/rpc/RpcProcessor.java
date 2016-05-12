package rpc;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;



public class RpcProcessor {
	private LinkedBlockingQueue<RpcResponse> responses;
	private ExecutorService executorService;
	private int executorThreadCount=10;
	private RpcServer server;
	public RpcProcessor (){
		executorService = Executors.newFixedThreadPool(executorThreadCount);
		responses=new LinkedBlockingQueue<>();
	}
	
	public RpcServer getServer() {
		return server;
	}

	public void setServer(RpcServer server) {
		this.server = server;
	}

	public LinkedBlockingQueue<RpcResponse> getResponses() {
		return responses;
	}

	public void setResponses(LinkedBlockingQueue<RpcResponse> responses) {
		this.responses = responses;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	public int getExecutorThreadCount() {
		return executorThreadCount;
	}

	public void setExecutorThreadCount(int executorThreadCount) {
		this.executorThreadCount = executorThreadCount;
	}
	
    public void ExcuteRemotecall(RpcRequest requst){
    	CallJob callJob= new CallJob(requst);
    	executorService.submit(callJob);
    }

	public class CallJob implements  Runnable {
		private RpcRequest requst;
		public CallJob(RpcRequest requst){
			this.requst=requst;
		}
		public void run() {

			Object obj = server.findService(requst.getClassName());
			if(obj!=null) {
				try {
					Method m = obj.getClass().getMethod(requst.getMethodName(), requst.getParameterTypes());
					Object result = m.invoke(obj, requst.getParameters());
					RpcResponse response = new RpcResponse();
					response.setRequestId(requst.getRequestId());
					response.setResult(result);
					responses.put(response);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			} else {
				throw new IllegalArgumentException("has no these class");
			}
		}
	}
}
