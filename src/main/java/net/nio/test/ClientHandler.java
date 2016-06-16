package net.nio.test;

import net.nio.AbstractNioHandler;
import net.nio.NioChannel;

public class ClientHandler  extends AbstractNioHandler {

	
	@Override
	public void channelOpened(NioChannel channel) {
		System.out.println("channelOpened  "+channel);
		channel.write("hello".getBytes());
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
		
		System.out.println("read ======================");
		System.out.println(new String(bytes));
		channel.close();
	}

}
