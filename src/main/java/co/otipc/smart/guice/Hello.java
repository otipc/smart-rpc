package co.otipc.smart.guice;

/**
 * Created by Chaoguo.Cui on 16/5/3.
 */
public class Hello implements IHello{
  public void sayHello(String userName) {
    System.out.println("Hello: "+userName);
  }
}
