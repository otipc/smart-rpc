package co.otipc.smart.guice;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Created by Chaoguo.Cui on 16/5/3.
 */
public class MyModule implements Module {
  public void configure(Binder binder) {
    binder.bind(IHello.class).to(Hello.class);
  }
}
