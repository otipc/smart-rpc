package net.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioChannel {
	private static final Logger LOG = LoggerFactory.getLogger(NioChannel.class);
	private long id;
	private NioProcessor processor;
	private SocketChannel socketChannel;
	private SelectionKey selectionKey;
	private volatile ChannelState state = ChannelState.OPEN ;    
	private Map<Object, Object>   attributes = new ConcurrentHashMap<Object, Object>();
	private final  Queue<ByteBuffer> writeBufferQueue = new ConcurrentLinkedQueue<>() ;
	private volatile long lastIoTime  = System.currentTimeMillis() ;
	
	public NioChannel(SocketChannel socketChannel){
		this.socketChannel = socketChannel;
		id=IdGenerator.getINSTNCE().getId();
	}
	
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public void setSelectionKey(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public long getId() {
		return id;
	}

	public NioProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(NioProcessor processor) {
		this.processor = processor;
	}
    
	public long getLastIoTime() {
		return lastIoTime;
	}

	public void setLastIoTime(long lastIoTime) {
		this.lastIoTime = lastIoTime;
	}

	public boolean isOpen() {
		return state == ChannelState.OPEN;
	}
	
	public boolean isClosing() {
		return state == ChannelState.CLOSING;
	}
	
	public boolean isClosed() {
		return state == ChannelState.CLOSED;
	}
	
	public void resume() {
		state = ChannelState.OPEN;
	}
	
	protected void close0() throws IOException {
		selectionKey.cancel();
		socketChannel.close();
	}
	
	public void close() {
		
		processor.remove(this);
	}
	
	boolean isValid() {		
		if (isClosing()) {
			return false;
		}
		
		if (isClosed()) {
			return false;
		}
		
		return true;
	}
	
	void setClosing() {
		this.state = ChannelState.CLOSING;
	}
	
	void setClosed() {
		this.state = ChannelState.CLOSED;
	}
	
	public Object getAttribute(Object key) {
		if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }

        return attributes.get(key);
	}
	
	public Object setAttribute(Object key, Object value) {
		if (key == null || value == null) {
			throw new IllegalArgumentException("key & value can not be null");
        }
        
        return attributes.put(key, value);
	}
	
	public boolean containsAttribute(Object key) {
		if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
		
		return attributes.containsKey(key);
	}
	
	public Object removeAttribute(Object key) {
		if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
		
		return attributes.remove(key);
	}

	
	boolean isReadable() {
		return isOpen() && selectionKey.isValid() && selectionKey.isReadable();
	}
	
	boolean isWritable() {
		return isOpen()  && selectionKey.isValid() && selectionKey.isWritable();
	}
	
	public Queue<ByteBuffer> getWriteBufferQueue() {
		return writeBufferQueue;
	}

	public  boolean write(byte[] data){
		if (isClosed())   { throw new RuntimeException("Channel is closed"); }
		if (isClosing())  { throw new RuntimeException("Channel is closing"); }
		if (data == null) { return false; }
		
		setLastIoTime(System.currentTimeMillis());
		getWriteBufferQueue().add(ByteBuffer.wrap(data));
		processor.setInterestedInWrite(this, true);
		return true;
	}
	
	protected int readTcp(ByteBuffer buf) throws IOException {
		return socketChannel.read(buf);
	}
	
	protected int writeTcp(ByteBuffer buf) throws IOException {
		return socketChannel.write(buf);
	}
	
	public String toString(){
		return "channel : "+id +" "+socketChannel;
	}
}
