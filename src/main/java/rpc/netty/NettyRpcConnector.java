package rpc.netty;

import java.io.IOException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import rpc.RpcConnector;
import rpc.RpcRequest;
import rpc.RpcResponse;

public class NettyRpcConnector implements RpcConnector {

	private String host;
	private int port;
	private Channel chanel;
	private RpcResponse response = new RpcResponse() ; 
	private Object lock = new Object();
	

	public void init() {
		// Configure the client.
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(
									 new LoggingHandler(LogLevel.INFO),
									new ObjectEncoder(), 
									new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
									new ObjectClientHandler(NettyRpcConnector.this.response,NettyRpcConnector.this.lock));
						}
					});

			// Start the client.
			ChannelFuture f = b.connect(host, port).sync();
			chanel = f.channel();
			// Wait until the connection is closed.
			//f.channel().closeFuture().sync();

		} catch (Exception e) {
			System.out.println("ssssssssssss");
			e.printStackTrace();
		} 
//		finally {
//			// Shut down the event loop to terminate all threads.
//			group.shutdownGracefully();
//		}
	}


	public void waitForResponse() {
		
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
			}
		}
	}
	@Override
	public void setHost(String host) {
		this.host = host;

	}

	@Override
	public void setport(int port) {
		this.port = port;

	}

	@Override
	public void start() throws IOException {
		init();

	}

	@Override
	public RpcResponse invoke(RpcRequest request) throws IOException {
		
		System.out.println("call ...");
		chanel.writeAndFlush(request);
		waitForResponse();
		return response;
	}


}
