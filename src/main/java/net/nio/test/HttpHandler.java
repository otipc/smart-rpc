package net.nio.test;

import net.nio.AbstractNioHandler;
import net.nio.NioChannel;

public class HttpHandler  extends AbstractNioHandler {

	
	private static final byte[] PM_HTTP_RESPONSE;
	 
	static {
		StringBuilder buf = new StringBuilder();
		buf.append("HTTP/1.1 200 OK\r\n");
		buf.append("Connection: keep-alive\r\n");
		buf.append("Content-Length: 2\r\n");
		buf.append("\r\n");
		buf.append("ok");
		
		PM_HTTP_RESPONSE = buf.toString().getBytes();
		
	}
	
	@Override
	public void channelOpened(NioChannel channel) {
		System.out.println("channelOpened  "+channel);
	}

	@Override
	public void channelThrown(NioChannel channel, Throwable e) {
		System.out.println("channelThrown  "+channel);
		e.printStackTrace();
	}

	@Override
	public void channelClosed(NioChannel channel) {
		System.out.println("channelClosed  "+channel);
	}

	@Override
	public void channelWritten(NioChannel channel, byte[] bytes) {
		System.out.println("channelWritten  "+channel);
	}

	@Override
	public void channelRead(NioChannel channel, byte[] bytes) {
//		System.out.println("read ======================");
//		System.out.println(new String(bytes));
		
		channel.write(PM_HTTP_RESPONSE);
		
//		channel.close();
	}

}
