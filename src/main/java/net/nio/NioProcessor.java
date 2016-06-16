package net.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NioProcessor {
	private static final Logger LOG = LoggerFactory.getLogger(NioProcessor.class);
	private final  Queue<NioChannel> newChannels  = new ConcurrentLinkedQueue<>();    
	private final  Queue<NioChannel> closingChannels  = new ConcurrentLinkedQueue<>();    
	private final  Queue<NioChannel> closedChannels  = new ConcurrentLinkedQueue<>();    
	private  NioHandler handler;
	private Selector selector;
	private volatile boolean shutdown=false;
	public  NioProcessor (NioHandler handler,int id){
		this.handler=handler;
		try {
			selector = Selector.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread(new RWThread(), "RWThread-NioProcessor"+id).start();
	}
	
	private void shutdown() throws IOException{
		
		selector.close();
	}
	public void stop(){
		shutdown=true;
	}
	
	public void add(NioChannel channel){
		newChannels.add(channel);
		selector.wakeup();
	}
	
	public void remove(NioChannel channel) {
    	if (this.shutdown) {
			throw new IllegalStateException("The processor is already shutdown!");
		}
		
		if (channel == null) {
			return;
		}
		
		scheduleClose(channel);
		selector.wakeup();
    }
	
	private void scheduleClose(NioChannel channel) {
		if (channel.isClosing() || channel.isClosed()) {
			return;
		}
		
		closingChannels.add(channel);
	}
	
	private void close() throws IOException {
	
		Iterator<NioChannel> iter= closingChannels.iterator();
		while (iter.hasNext()) {
			NioChannel channel = iter.next();
			if(channel.getWriteBufferQueue().isEmpty()) {
				closedChannels.add(channel);
				iter.remove();
			} 
		}
		
		for (NioChannel channel = closedChannels.poll(); channel != null; channel = closedChannels.poll()) {
			if (channel.isClosed()) {
				LOG.debug("Skip close because it is already closed, |channel={}|", channel);
				continue;
			}
			channel.setClosing();
			LOG.debug("Closing |channel={}|", channel);
			// fire channel closed event
			fireChannelClosed(channel);
			close(channel);
			channel.setClosed();
			
			LOG.debug("Closed |channel={}|" + channel);
		}
	}
	
	private void close(NioChannel channel) throws IOException {
		try {
			channel.close0();		
			
		} catch (Exception e) {
			LOG.warn("Catch close exception and fire it, |channel={}|", channel, e);
			fireChannelThrown(channel, e);
		}
	}
	private void register() throws ClosedChannelException {
		for (NioChannel channel = newChannels.poll(); channel != null; channel = newChannels.poll()) {
			SelectableChannel sc = channel.getSocketChannel();
			SelectionKey key = sc.register(selector, SelectionKey.OP_READ, channel);
			channel.setSelectionKey(key);
			// fire channel opened event
			fireChannelOpened(channel);
		}
	}
	
	private void process0(NioChannel channel) throws IOException {
		// set last IO time
		channel.setLastIoTime(System.currentTimeMillis());
		
		// Process reads
		if (channel.isReadable()) {
			LOG.debug("Read event process on |channel={}|", channel);
			read(channel);
		}

		// Process writes
		if (channel.isWritable()) {
			LOG.debug("Write event process on |channel={}|", channel);
			write(channel);
		}
	}
	
	private void read(NioChannel channel){
		ByteBuffer buf = ByteBuffer.allocate(4096);
		int readBytes = 0;
		try {
			int ret;
			while ((ret = channel.readTcp(buf)) > 0) {
				readBytes += ret;
				if (!buf.hasRemaining()) {
					break;
				}
			}

			if (readBytes > 0) {
				LOG.debug("Actual |readBytes={}|{}", readBytes,channel);
				fireChannelRead(channel, buf, readBytes);
			}

			// read end-of-stream, remote peer may close channel so close channel.
			if (ret < 0) {
				scheduleClose(channel);
			}
			
		} catch (Exception e) {
			LOG.debug("Catch read exception and fire it, |channel={}|", channel, e);

			// fire exception caught event
			fireChannelThrown(channel, e);
			
			// if it is IO exception close channel avoid infinite loop.
			if (e instanceof IOException) {
				scheduleClose(channel);
			}
		} finally {
			if (readBytes > 0) { buf.clear(); }
		}
	}
	
	private void write(NioChannel channel) throws IOException{
		Queue<ByteBuffer> writeQueue = channel.getWriteBufferQueue();
		// First set not be interested to write event
		setInterestedInWrite(channel, false);
		
		ByteBuffer buf = writeQueue.peek();
		if (buf == null) {
			return;
		}
		
		write(channel, buf, buf.remaining());
		
		if (buf.hasRemaining()) {
			setInterestedInWrite(channel, true);
			return;
		} else {
			writeQueue.remove();
			
			// fire channel written event
			fireChannelWritten(channel, buf);
		}
		
		if (!writeQueue.isEmpty()) {
			setInterestedInWrite(channel, true);
		}
	}
	
	private int write(NioChannel channel, ByteBuffer buf, int maxLength) throws IOException {		
		int writtenBytes = 0;
		LOG.debug(" Allow write max len={}, Waiting write byte buffer={}", maxLength, buf); 

		if (buf.hasRemaining()) {
			int length = Math.min(buf.remaining(), maxLength);
			writtenBytes = writeTcp(channel, buf, length);
			
		}
		
		LOG.debug(" Actual written byte size, |writtenBytes={}|", writtenBytes);
		return writtenBytes;
	}
	
	private int writeTcp(NioChannel channel, ByteBuffer buf, int length) throws IOException {
		if (buf.remaining() <= length) {
			return channel.writeTcp(buf);
		}

		int oldLimit = buf.limit();
		buf.limit(buf.position() + length);
		try {
			return channel.writeTcp(buf);
		} finally {
			buf.limit(oldLimit);
		}
	}
	
	protected void setInterestedInWrite(NioChannel channel, boolean isInterested) {
		SelectionKey key = channel.getSelectionKey();
		if (key == null || !key.isValid()) {
			System.out.println("error");
			return;
		}
		
		int oldInterestOps = key.interestOps();
		int newInterestOps = oldInterestOps;
		if (isInterested) {
			newInterestOps |= SelectionKey.OP_WRITE;
		} else {
			newInterestOps &= ~SelectionKey.OP_WRITE;
		}

		if (oldInterestOps != newInterestOps) {
            key.interestOps(newInterestOps);
        }
		
		selector.wakeup();
	}
	 private void fireChannelOpened(NioChannel channel) {
	    	new NioChannelEvent(channel,ChannelEventType.CHANNEL_OPENED, handler).fire();
	    }
	 private void fireChannelClosed(NioChannel channel) {
		 new NioChannelEvent(channel,ChannelEventType.CHANNEL_CLOSED, handler).fire();
	 }
	 private void fireChannelThrown(NioChannel channel,Exception e) {
		 new NioChannelEvent(channel,ChannelEventType.CHANNEL_THROWN, handler,e).fire();
	 }
	 private void fireChannelRead(NioChannel channel,ByteBuffer buf, int length) {
		 byte[] barr = new byte[length];
		 System.arraycopy(buf.array(), 0, barr, 0, length);
		 new NioChannelEvent(channel,ChannelEventType.CHANNEL_READ, handler,barr).fire();
	 }
	 private void fireChannelWritten(NioChannel channel,ByteBuffer buf) {
		 new NioChannelEvent(channel,ChannelEventType.CHANNEL_WRITTEN, handler,buf.array()).fire();
	 }
	 
	private class RWThread implements Runnable {
		public void run() {
			while (!shutdown) {
				try {
					int selected = selector.select(1000);
					register();
					if(selected>0 ) {
					Iterator<SelectionKey> it = selector.selectedKeys().iterator();
					while (it.hasNext()) {
						NioChannel channel = (NioChannel) it.next().attachment();
						if (channel.isValid()) {
							process0(channel);
						} else {
							LOG.debug("Channel is invalid, |channel={}|", channel);
						}
						it.remove();
					}
					}
					
					close();
					
				
				} catch (Exception e) {
					LOG.error(" Process exception", e);
				}
			}
			
			// if shutdown == true, we shutdown the processor
			if (shutdown) {
				try {
					shutdown();
				} catch (Exception e) {
					LOG.error("[CRAFT-ATOM-NIO] Shutdown exception", e);
				}
			}
		}
	}
}
