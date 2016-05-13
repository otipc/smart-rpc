package rpc.netty;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import rpc.RpcResponse;

public class ObjectClientHandler extends  SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger logger = Logger.getLogger(
            ObjectClientHandler.class.getName());
	private RpcResponse response;
    private Object lock;

   public ObjectClientHandler(RpcResponse response,Object lock){
	   this.response=response;
	   this.lock = lock;
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
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
		
		response.setResult(msg.getResult());
		synchronized (lock) {
			lock.notifyAll();
			System.out.println("notifyAll");
		}
		
	     System.out.println("client  recv "+msg);
	}
}
