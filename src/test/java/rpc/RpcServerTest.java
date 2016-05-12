package rpc;

import org.junit.Before;
import org.junit.Test;

import co.otipc.smart.guice.Hello;
import co.otipc.smart.guice.IHello;



public class RpcServerTest {
  private	RpcServer server =null;
  
  @Before
 public void init(){
	 this.server=new RpcServer();
 }
  
  @Test
  public  void Testregester(){
	  IHello h = new Hello();
	  server.export(IHello.class, h);
//	  server.export(IHello.class, h);
//	  server.export(Hello2.class, h);
  }
  
}
