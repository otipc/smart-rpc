package rpc;

import org.junit.Before;
import org.junit.Test;

import co.otipc.smart.guice.Hello;
import co.otipc.smart.guice.Hello2;
import co.otipc.smart.guice.IHello;



public class RpcProviderTest {
  private	RpcProvider rpcProvider =null;
  
  @Before
 public void init(){
	 this.rpcProvider=new RpcProvider();
 }
  
  @Test
  public  void Testregester(){
	  IHello h = new Hello();
	  rpcProvider.regester(IHello.class, h);
//	  rpcProvider.regester(IHello.class, h);
//	  rpcProvider.regester(Hello2.class, h);
  }
  
}
