# simple-spring
A simple IOC container refer to Spring.

## quick-start

* [version 1.0](#version 10)
    * [简单的 IOC](#简单的 IOC)
    * [简单的 AOP](#简单的 AOP)
* [version 2.0](#version 20)

## version 1.0

### 简单的 IOC

最简单的 IOC 容器只需以下几步即可实现：

1. 加载 xml 配置文件，遍历其中的标签
2. 获取标签中的 id 和 class 属性，加载 class 属性对应的类，并创建 bean
3. 遍历标签中的标签，获取属性值，并将属性值填充到 bean 中
4. 将 bean 注册到 bean 容器中

先对 version 1.0 代码结构做一个简要介绍：

- SimpleIOC：IOC 的实现类，实现了上面所说的几个步骤
- SimpleIOCTest：IOC 的测试类
- Car：IOC 测试使用的 bean
- Wheel：测试使用的 bean
- ioc.xml：bean 配置文件

### 简单的 AOP

AOP 是基于代理模式的，在介绍 AOP 的具体实现之前，先引入 Spring AOP 中的一些概念：

**通知(Advice)**——定义了要植入对象的逻辑以及执行时机，Spring 中对应5种不同类型的通知：

1. 前置通知（Before）：在目标方法执行前，执行通知
2. 后置通知（After）：在目标方法执行后，执行通知，此时不关系目标方法返回的结果是什么
3. 返回通知（After-returning）：在目标方法执行后，执行通知
4. 异常通知（After-throwing）：在目标方法抛出异常后执行通知
5. 环绕通知（Around）: 目标方法被通知包裹，通知在目标方法执行前和执行后都被会调用

**切点(Pointcut)**——定义了在何处执行通知。作用是通过匹配规则查找合适的连接点(Joinpoint)，AOP 会在这些连接点上织入通知

**切面(Aspect)**——包含了通知和切点，通知和切点共同定义了切面是什么，在何时，何处执行切面逻辑

说完概念，接下来我们来说说简单 AOP 实现的步骤。这里 AOP 是基于 JDK 动态代理实现的，只需3步即可完成：

1. 定义一个包含切面逻辑的对象，这里假设叫 logMethodInvocation
2. 定义一个 Advice 对象（实现了 InvocationHandler 接口），并将上面的 logMethodInvocation 和目标对象传入
3. 将上面的 Adivce 对象和目标对象传给 JDK 动态代理方法，为目标对象生成代理

上面步骤比较简单，不过在实现过程中，要引入一些辅助接口才能实现。接下来介绍一下简单 AOP 的代码结构：

- MethodInvocation 接口——实现类包含了切面逻辑，如 logMethodInvocation
- Advice 接口——继承了 InvocationHandler 接口
- BeforeAdvice 类——实现了 Advice 接口，是一个前置通知
- SimpleAOP 类——生成代理类
- SimpleAOPTest——SimpleAOP 的测试类
- HelloService 接口——目标对象接口
- HelloServiceImpl——目标对象

**不过以上代码只是实现了一个超级简单的 IOC 容器和 AOP 代理，且只能独立运行。在接下来的 version 2.0 打算实现一个较为复杂的 IOC 和 AOP。version 1.0 的详细代码见 [simple-spring-version1.0](https://github.com/hzcforever/simple-spring/tree/master/version1.0)**

## version 2.0

**待续...**
