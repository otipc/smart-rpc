package rpc;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class RpcProcessor {
	private ConcurrentLinkedQueue<RpcResponse> responses;
	private ExecutorService executorService;
	private int executorThreadCount=10;
	private RpcServer server;
	public RpcProcessor (){
		executorService = Executors.newFixedThreadPool(executorThreadCount);
		responses=new ConcurrentLinkedQueue<>();
	}
	
	public RpcServer getServer() {
		return server;
	}

	public void setServer(RpcServer server) {
		this.server = server;
	}

	public ConcurrentLinkedQueue<RpcResponse> getResponses() {
		return responses;
	}

	public void setResponses(ConcurrentLinkedQueue<RpcResponse> responses) {
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
	
    public void ExcuteRemotecall(RpcRequst requst){
    	CallJob callJob= new CallJob(requst);
    	executorService.submit(callJob);
    }

	public class CallJob implements  Runnable {
		private RpcRequst requst;
		public CallJob(RpcRequst requst){
			this.requst=requst;
		}
		public void run() {

			Object obj = server.findService(requst.getClassName());
			if(obj!=null) {
				try {
					Method m = obj.getClass().getMethod(requst.getMethodName(), requst.getParameterTypes());
					Object result = m.invoke(obj, requst.getParameters());
					RpcResponse response = new RpcResponse();
					response.setResult(result);
					responses.offer(response);
				} catch (Throwable th) {
					th.printStackTrace();
				}
			} else {
				throw new IllegalArgumentException("has no these class");
			}
		}
	}
}
