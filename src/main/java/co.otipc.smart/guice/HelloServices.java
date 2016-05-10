package co.otipc.smart.guice;

import com.google.inject.Inject;

/**
 * Created by Chaoguo.Cui on 16/5/3.
 */
public class HelloServices {
  private IHello hello = null;

  @Inject
  public void helloServicessdf(IHello hello) {
    this.hello = hello;
  }

  public void sayHello() {
    hello.sayHello("info test");
  }
}
