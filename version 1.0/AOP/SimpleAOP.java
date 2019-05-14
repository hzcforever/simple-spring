package com.hzc.IOC_AOP.AOP;

import java.lang.reflect.Proxy;

/**
 * MethodInvocation接口  实现类包含了切面逻辑，如上面的 logMethodInvocation
 * Advice接口  继承了 InvocationHandler 接口
 * BeforeAdvice类  实现了 Advice 接口，是一个前置通知
 * SimpleAOP类  生成代理类
 * SimpleAOPTest  SimpleAOP 测试类
 * HelloService接口  目标对象接口
 * HelloServiceImpl  目标对象
 */

public class SimpleAOP {
    public static Object getProxy(Object bean, Advice advice) {
        return Proxy.newProxyInstance(SimpleAOP.class.getClassLoader(),
                bean.getClass().getInterfaces(), advice);
    }
}
