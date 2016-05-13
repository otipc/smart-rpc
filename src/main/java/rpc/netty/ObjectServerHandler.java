package rpc.netty;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import rpc.RpcRequest;

public class ObjectServerHandler extends  SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = Logger.getLogger(
    		ObjectServerHandler.class.getName());
   private NettyRpcAcceptor acceptor;
    
    public   ObjectServerHandler(NettyRpcAcceptor acceptor){
    	this.acceptor=acceptor;
    }
    
    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    	System.out.println("channelUnregistered");
        ctx.fireChannelUnregistered();
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Send the first message if this handler is a client-side handler.
    	System.out.println("channelActive");
    }

    

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.log(
                Level.INFO,
                "Unexpected exception from downstream.", cause);
        ctx.close();
    }

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
		
		System.out.println("server  recv "+msg);
		acceptor.getProcessor().ExcuteRemotecall(msg);
		acceptor.getChannels().put(msg.getRequestId(), ctx);
	}
}
