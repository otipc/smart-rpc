package rpc.netty;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import rpc.RpcAcceptor;
import rpc.RpcProcessor;
import rpc.RpcResponse;

public class  NettyRpcAcceptor implements RpcAcceptor{
	
    private String host;
    private int port;
    private RpcProcessor processor;
    protected ConcurrentHashMap<String, ChannelHandlerContext> channels = new ConcurrentHashMap<>();
    EventLoopGroup bossGroup;
    EventLoopGroup workerGroup;
  	
	public void init() throws IOException {
		 // Configure the server.
         bossGroup = new NioEventLoopGroup();
         workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .option(ChannelOption.SO_BACKLOG, 100)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(
                             new LoggingHandler(LogLevel.INFO),
                    		  new ObjectEncoder(),
                              new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                              new ObjectServerHandler(NettyRpcAcceptor.this));
                 }
             });

            // Start the server.
            ChannelFuture f = b.bind(host,port).sync();
            System.out.println("started and listen on");
            // Wait until the server socket is closed.
           // f.channel().closeFuture().sync();
            System.out.println("server start...");
        }catch(Exception e) {
        	e.printStackTrace();
        } 
//        finally {
//            // Shut down all event loops to terminate all threads.
//            bossGroup.shutdownGracefully();
//            workerGroup.shutdownGracefully();
//        }
	}
	
	@Override
	public void start() throws IOException {
		this.init();
		
		// 取出response
		processor.getExecutorService().execute(new Runnable() {
			public void run() {
				while (true) {
					try {
						RpcResponse response = processor.getResponses().take();
						if (response != null) {
							write(response);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}


	@Override
	public void stop() throws IOException {
		 bossGroup.shutdownGracefully();
         workerGroup.shutdownGracefully();
	}


	@Override
	public void setHost(String host) {
		this.host=host;
	}

	@Override
	public void setport(int port) {
		this.port=port;
	}


	public ConcurrentHashMap<String, ChannelHandlerContext> getChannels() {
		return channels;
	}

	public void setChannels(ConcurrentHashMap<String, ChannelHandlerContext> channels) {
		this.channels = channels;
	}

	public RpcProcessor getProcessor() {
		return processor;
	}

	@Override
	public void write(RpcResponse response) throws IOException {
		ChannelHandlerContext ctx = channels.get(response.getRequestId());
		channels.remove(response.getRequestId());
		ctx.writeAndFlush(response);
	}


	@Override
	public void setProcessor(RpcProcessor processor) {
		this.processor=processor;
	}

}
