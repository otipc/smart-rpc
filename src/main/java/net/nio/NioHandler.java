package net.nio;

import java.io.IOException;

public interface NioHandler {

	/**
	 * 连接建立成功的通知事件
	 * 
	 */
	public void channelOpened(NioChannel channel) ;

	/**
	 * 连接失败
	 * 
	 */
	public void channelThrown(NioChannel channel, Throwable e);
	
	/**
	 * 连接关闭通知
	 */
	public void channelClosed(NioChannel channel);

	/**
	 * 收到数据需要处理
	 * 
	 */
	void channelWritten(NioChannel channel, byte[] bytes);
	/**
	 * 写入数据需要处理
	 * @throws IOException 
	 * 
	 */
	void channelRead(NioChannel channel, byte[] bytes) ;

}