package net.nio;

public class NioProcessorPool {
	 private final NioProcessor[] pool ;
	 
	 public NioProcessorPool (NioHandler handler){
		int size= Runtime.getRuntime().availableProcessors();
		pool = new NioProcessor[size];
		for (int i = 0; i < size; i++) {
			pool[i] = new NioProcessor(handler,i+1);
		}
	 }
	 
	 public void shutdown() {
			for (int i = 0; i < pool.length; i++) {
				pool[i].stop();
			}
		}
	 
	 public NioProcessor pick(NioChannel channel) {
			return pool[Math.abs((int) (channel.getId() % pool.length))];
		}
}
