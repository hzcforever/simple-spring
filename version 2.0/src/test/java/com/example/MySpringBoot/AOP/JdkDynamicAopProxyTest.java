package com.example.MySpringBoot.AOP;

import com.example.MySpringBoot.IOC.XML.XmlBeanFactory;
import com.example.MySpringBoot.Model.HelloService;
import com.example.MySpringBoot.Model.HelloServiceImpl;
import com.example.MySpringBoot.Model.LogInterceptor;
import org.junit.Test;
import java.lang.reflect.Method;

public class JdkDynamicAopProxyTest {

    @Test
    public void getProxy() throws Exception {
        System.out.println("---------- no proxy ----------");
        HelloService helloService = new HelloServiceImpl();
        helloService.sayHelloWorld();

        System.out.println("\n----------- proxy -----------");
        AdvisedSupport advisedSupport = new AdvisedSupport();
        advisedSupport.setMethodInterceptor(new LogInterceptor());

        TargetSource targetSource = new TargetSource(
                helloService, HelloServiceImpl.class, HelloServiceImpl.class.getInterfaces());
        advisedSupport.setTargetSource(targetSource);
        advisedSupport.setMethodMatcher((Method method, Class beanClass) -> true);

        helloService = (HelloService) new JdkDynamicAopProxy(advisedSupport).getProxy();
        helloService.sayHelloWorld();
    }
}
