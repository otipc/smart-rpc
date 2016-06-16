package co.otipc.smart.netty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

public class HttpClientInboundHandler extends ChannelInboundHandlerAdapter {
	 private static Log log = LogFactory.getLog(HttpClientInboundHandler.class);
  @Override public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof HttpResponse) {
      HttpResponse response = (HttpResponse) msg;
     // System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaderNames.CONTENT_TYPE));
    }
    if (msg instanceof HttpContent) {
      HttpContent content = (HttpContent) msg;
      ByteBuf buf = content.content();
   //   System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
      buf.release();
    }
    
  }
  
  @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
	    log.error(cause.getMessage());
	    ctx.close();
	  }
  
}
