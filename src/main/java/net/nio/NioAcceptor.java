package net.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioAcceptor {
	private static final Logger LOG = LoggerFactory.getLogger(NioAcceptor.class);
	protected   SelectableChannel boundsc  ;
    private Selector selector;
    private boolean selectable=false;
    private SocketAddress address;
    private  NioHandler handler;
    private  NioProcessorPool pool ;
    
    public  NioAcceptor(NioHandler handler){
    	this.handler=handler;
    }
	public void bind(int port)throws IOException{
		this.bind(new InetSocketAddress(port));
	}
	
	void bind(SocketAddress address)throws IOException{		
		if (!selectable) {
			this.address = address;
			init();
		}
		else{
			LOG.error("address is binded {}", this.address);
		}
	}
	
	private void init() throws IOException{
		selector = Selector.open();
		selectable = true;
		bindByProtocol(address);
		pool = new NioProcessorPool(handler);
		new Thread(new AcceptThread(), "AcceptThread").start();
	}
	
	protected  void bindByProtocol(SocketAddress address) throws IOException{
		ServerSocketChannel ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ServerSocket ss = ssc.socket();
		ss.setReuseAddress(true);
		
		ss.bind(address,128);
		
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		boundsc=ssc;
	}
	
	private void close(SelectableChannel sc) throws IOException {
		if (sc != null) {
			SelectionKey key = sc.keyFor(selector);
			if (key != null) {
				key.cancel();
			}
			try {
				sc.close();
			} catch (IOException ex) {
				LOG.error(" Close exception", ex);
			}
		}
		
		
	}
	
	public void stop(){
		selectable = false;
	}
	
	private void shutdown() throws IOException {
	
		close(boundsc);
		// close acceptor selector
		this.selector.close();
		LOG.debug("Shutdown acceptor successful");
	}
	protected NioChannel acceptByProtocol(SelectionKey key) throws IOException {
		if (key == null || !key.isValid() || !key.isAcceptable()) {
            return null;
        }		
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		SocketChannel sc = null;
		try {
			sc = ssc.accept();
			if (sc == null)     {            return null; }
			
			sc.configureBlocking(false);
			sc.socket().setKeepAlive(true);
			NioChannel channel = new NioChannel(sc);
			NioProcessor processor = pool.pick(channel);
			channel.setProcessor(processor);
			processor.add(channel);
			return channel;
		} catch (IOException e) {
			close(sc);
			throw e;
		}
	}
	
	private void accept() throws IOException {
		Iterator<SelectionKey> it = selector.selectedKeys().iterator();
		while (it.hasNext()) {
			SelectionKey key = it.next();
			it.remove();
			acceptByProtocol(key);
		}
	}
	
	private class AcceptThread implements  Runnable {
		public void run() {
			while (selectable) {
				try {
					int selected = selector.select();
					
					if (selected > 0) {
						accept();
					}
				} catch (Exception e) {
					LOG.error("Unexpected exception", e);
				}
			}
			
			try {
				shutdown();
			} catch (Exception e) {
				LOG.error("Shutdown exception", e);
			}
		}
	}
}
