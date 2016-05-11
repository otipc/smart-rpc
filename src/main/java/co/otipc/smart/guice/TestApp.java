package co.otipc.smart.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Created by Chaoguo.Cui on 16/5/3.
 */
public class TestApp {
  public static void main(String[] args) {
    HelloServices helloServices = new HelloServices(); //服务声明
    Module module = new MyModule(); //声明控制模块

    Injector in = Guice.createInjector(module); //控制注入
    in.injectMembers(helloServices); //注入的成员对象
    helloServices.sayHello();
  }
}
