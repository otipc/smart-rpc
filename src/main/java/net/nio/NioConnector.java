package net.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioConnector {
	private static final Logger LOG = LoggerFactory.getLogger(NioConnector.class);
	private NioHandler handler;
	private CountDownLatch latch ;
	private Selector selector;
	private NioChannel channel;
	private NioProcessor processor;
	 public  NioConnector(NioHandler handler){
	    	this.handler=handler;
	    	latch = new CountDownLatch(1);
	    	
	    }
		public NioChannel connect(String host,int port)throws IOException{
			return this.connect(new InetSocketAddress(host,port));
		}
		
		public NioChannel connect(SocketAddress address)throws IOException{		
			selector = Selector.open();
			return connectByProtocol(address);
		}
		
		public void finishConnect(){
			try {
				latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void close() {
			channel.close();
			processor.stop();
		}
		
		
		private NioChannel connectByProtocol(SocketAddress address) throws IOException{
			SocketChannel sc = SocketChannel.open();
			sc.socket().setKeepAlive(true);
			sc.configureBlocking(false);			
			channel = new NioChannel(sc);
			sc.connect(address);
			sc.register(selector, SelectionKey.OP_CONNECT, channel);
			processor = new NioProcessor(handler, 1);
			channel.setProcessor(processor);
			processor.add(channel);
			
			new Thread(new ConnectThread(), "ConnectThread").start();
			return channel;
		}
		
		private class ConnectThread implements  Runnable {
			
			public void run() {
				try {
					selector.select();
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						SelectionKey key = it.next();
						it.remove();
						//
						if (key.isConnectable()) {
							SocketChannel sc = (SocketChannel) key.channel();
							if(sc.isConnectionPending()){
								sc.finishConnect();
								
							}
						}
						LOG.debug("finish Connect");
						selector.close();
						latch.countDown();
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		}
}
