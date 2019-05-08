# simple-spring
A simple IOC container refer to Spring.

## quick-start

* [Spring 部分配置特性](#Spring-部分配置特性)
	* [id 和 name](#id-和-name)
	* [配置是否允许 Bean 覆盖和循环依赖](#配置是否允许-Bean-覆盖和循环依赖)
	* [profile](#profile)
	* [工厂模式生成 Bean](#工厂模式生成-Bean)
	* [FactoryBean](#FactoryBean)
	* [初始化 Bean 的回调](#初始化-Bean-的回调)
	* [销毁 Bean 的回调](#销毁-Bean-的回调)
	* [Bean 的继承](#Bean-的继承)
	* [方法注入](#方法注入)
	* [BeanPostProcessor](#BeanPostProcessor)
	* [BeanWrapper](#BeanWrapper)
* [version 1.0](#version-10)
    * [简单的 IOC](#简单的-IOC)
    * [简单的 AOP](#简单的-AOP)
* [version 2.0](#version-20)

## Spring 部分配置特性

### id 和 name

每个 Bean 在 Spring 容器中都有一个唯一的名字（beanName）和 0 个或多个别名（aliases）。

我们从 Spring 容器中获取 Bean 的时候，可以根据 beanName，也可以通过别名。

    beanFactory.getBean("beanName or alias");

在配置 <bean /> 的过程中，我们可以配置 id 和 name，看几个例子就知道是怎么回事了。

    <bean id="admin" name="m1, m2, m3" class="com.test.Admin" />

以上配置的结果是：beanName 为 admin，别名有 3 个，分别为 m1、m2、m3。

    <bean name="m1, m2, m3" class="com.test.Admin" />

以上配置的结果是：beanName 为 m1，别名有 2 个，分别为 m2、m3。

    <bean class="com.test.Admin" />

以上配置的结果是：beanName 为 com.test.Admin#0，别名为： com.test.Admin

    <bean id="admin" class="com.test.Admin" />

以上配置的结果是：beanName 为 admin，没有别名。

### 配置是否允许 Bean 覆盖和循环依赖

我们说过，默认情况下，allowBeanDefinitionOverriding 属性为 null。如果在同一配置文件中 Bean id 或 name 重复了，会抛出异常，但是如果不是同一配置文件中，会发生覆盖。

可是有些时候我们希望在系统启动的过程中就严格杜绝发生 Bean 覆盖，因为万一出现这种情况，会增加我们排查问题的成本。

循环依赖说的是 A 依赖 B，而 B 又依赖 A；或者是 A 依赖 B，B 依赖 C，而 C 却依赖 A。默认 allowCircularReferences 也是 null。

它们两个属性是一起出现的，必然可以在同一个地方一起进行配置。

添加这两个属性的作者 Juergen Hoeller 在这个 jira 的讨论中说明了怎么配置这两个属性。

    public class NoBeanOverridingContextLoader extends ContextLoader {
    
        @Override
        protected void customizeContext(ServletContext servletContext, ConfigurableWebApplicationContext applicationContext) {
            super.customizeContext(servletContext, applicationContext);
    	    AbstractRefreshableApplicationContext arac = (AbstractRefreshableApplicationContext) applicationContext;
    	    arac.setAllowBeanDefinitionOverriding(false);
        }
    }

    public class MyContextLoaderListener extends org.springframework.web.context.ContextLoaderListener {
    
        @Override
        protected ContextLoader createContextLoader() {
            return new NoBeanOverridingContextLoader();
        }
    
    }

    <listener>
        <listener-class>com.javadoop.MyContextLoaderListener</listener-class>  
    </listener>

如果以上方式不能满足你的需求，请参考这个链接：[解决spring中不同配置文件中存在name或者id相同的bean可能引起的问题](https://blog.csdn.net/zgmzyr/article/details/39380477)

### profile

我们可以把不同环境的配置分别配置到单独的文件中，举个例子：

    <beans profile="development"
    	xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    	xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    	xsi:schemaLocation="...">
    
    	<jdbc:embedded-database id="dataSource">
    		<jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
    		<jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
    	</jdbc:embedded-database>
    </beans>

    <beans profile="production"
    	xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    	xmlns:jee="http://www.springframework.org/schema/jee"
    	xsi:schemaLocation="...">
    
    	<jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
    </beans>

应该不必做过多解释了吧，看每个文件第一行的 profile=""。

当然，我们也可以在一个配置文件中使用：

    <beans xmlns="http://www.springframework.org/schema/beans"
    	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    	xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    	xmlns:jee="http://www.springframework.org/schema/jee"
    	xsi:schemaLocation="...">
    
    	<beans profile="development">
    		<jdbc:embedded-database id="dataSource">
    			<jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
    			<jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
    		</jdbc:embedded-database>
    	</beans>
    
    	<beans profile="production">
    		<jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
    	</beans>

    </beans>

理解起来也很简单吧。

接下来的问题是，怎么使用特定的 profile 呢？Spring 在启动的过程中，会去寻找 “spring.profiles.active” 的属性值，根据这个属性值来的。那怎么配置这个值呢？

Spring 会在这几个地方寻找 spring.profiles.active 的属性值：操作系统环境变量、JVM 系统变量、web.xml 中定义的参数、JNDI。

最简单的方式莫过于在程序启动的时候指定：`-Dspring.profiles.active="profile1,profile2"`

profile 可以激活多个

当然，我们也可以通过代码的形式从 Environment 中设置 profile：

    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.getEnvironment().setActiveProfiles("development");
    ctx.register(SomeConfig.class, StandaloneDataConfig.class, JndiDataConfig.class);
    ctx.refresh(); // 重启

如果是 Spring Boot 的话更简单，我们一般会创建 application.properties、application-dev.properties、application-prod.properties 等文件，其中 application.properties 配置各个环境通用的配置，application-{profile}.properties 中配置特定环境的配置，然后在启动的时候指定 profile：

    java -Dspring.profiles.active=prod -jar JavaDoop.jar

如果是单元测试中使用的话，在测试类中使用 @ActiveProfiles 指定，这里就不展开了。

### 工厂模式生成 Bean

请注意 factory-bean 和 FactoryBean 的区别。这节说的是前者，指的是静态工厂或实例工厂，而后者是 Spring 中的特殊接口，代表一类特殊的 Bean，下面一节会介绍 FactoryBean。

设计模式里，工厂方法模式分静态工厂和实例工厂，我们分别看看 Spring 中怎么配置这两个，来个代码示例就什么都清楚了。

静态工厂：

    <bean id="clientService" class="examples.ClientService" factory-method="createInstance"/>

    public class ClientService {
    	private static ClientService clientService = new ClientService();
    	private ClientService() {}
    
    	// 静态方法
    	public static ClientService createInstance() {
    		return clientService;
    	}
    }

实例工厂：

    <bean id="serviceLocator" class="examples.DefaultServiceLocator">
    	<!-- inject any dependencies required by this locator bean -->
    </bean>
    
    <bean id="clientService" factory-bean="serviceLocator" factory-method="createClientServiceInstance"/>
    
    <bean id="accountService" factory-bean="serviceLocator" factory-method="createAccountServiceInstance"/>
    
    public class DefaultServiceLocator {
    	private static ClientService clientService = new ClientServiceImpl();
    
    	private static AccountService accountService = new AccountServiceImpl();
    
    	public ClientService createClientServiceInstance() {
    		return clientService;
    	}
    
    	public AccountService createAccountServiceInstance() {
    		return accountService;
    	}
    }
    
### FactoryBean

FactoryBean 以 Bean 结尾，表示它是一类 Bean，不同于普通 Bean 的是：它是实现了 FactoryBean<T> 接口的 Bean，当在 IOC 容器中的 Bean 实现了 FactoryBean 接口后，通过 getBean(String BeanName) 方法从 BeanFactory 中获取到的 Bean 对象并不是 FactoryBean 的实现类对象，实际上是通过 FactoryBean 的 getObject() 返回的对象。如果要获取 FactoryBean 对象，需要在 beanName 前面加一个 & 符号来获取。

    <bean id="school" class="com.test.School">
        <property name="schoolName" value="hit"/>
        <property name="address" value="harbin"/>
    </bean>
    <bean id="factoryBean" class="com.test.FactoryBeanTest">
        <property name="type" value="school"/>
    </bean>

下面通过一个类实现 FactoryBean 接口，在配置文件中将该类的 type 属性设置为 student，会在 getObject() 方法中返回 Student 对象。

    public class FactoryBeanTest implements FactoryBean {
    
    	private String type;
    
    	public String getType() {
            return type;
    	}
    
    	public void setType(String type) {
            this.type = type;
    	}
    
    	public Object getObject() throws Exception {
    	    if ("student".equals(type)) {
                return new Student();
    	    } else {
                return new School();
    	    }
    	}
    
    	public Class<?> getObjectType() {
            return School.class;
    	}
    
    	public boolean isSingleton() {
            return true;
    	}
    }

通过测试可以验证之前的想法。
 
    School school = (School) applicationContext.getBean("factoryBean");
	FactoryBeanTest factoryBean = (FactoryBeanTest) applicationContext.getBean("&factoryBean");
    System.out.println(school.getClass().getName());
    System.out.println(factoryBean.getClass().getName());

测试结果：

	com.test.School 
	com.test.FactoryBeanTest

所以从 IOC 容器获取实现了 FactoryBean 的实现类时，返回的是实现类中的 getObject 方法返回的对象，要想获取 FactoryBean 的实现类，得在 getBean 中的 BeanName 前加上 & ,即 getBean(String &BeanName)。

### BeanWrapper

BeanWrapper 接口，作为 spring 内部的一个核心接口，正如其名，它是 bean 的包裹类,即在内部中将会保存该 bean 的实例，提供其它一些扩展功能。同时 BeanWrapper 接口还继承了 PropertyAccessor, propertyEditorRegistry, TypeConverter、ConfigurablePropertyAccesso r接口，所以它还提供了访问 bean 的属性值、属性编辑器注册、类型转换等功能。

    School school = new School();
    BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(school);
    PropertyValue schoolName = new PropertyValue("schoolName", "HIT");
    PropertyValue address = new PropertyValue("address", "Harbin");
    beanWrapper.setPropertyValue(schoolName);
    beanWrapper.setPropertyValue(address);
    System.out.println(beanWrapper.getWrappedInstance());

上面的代码已经很清楚地演示了如何使用 BeanWrapper 设置和获取 bean 的属性。 

### 初始化 Bean 的回调

有以下四种方案：

    <bean id="exampleInitBean" class="examples.ExampleBean" init-method="init"/>

    public class AnotherExampleBean implements InitializingBean {
    
    	public void afterPropertiesSet() {
    	// do some initialization work
    	}
    }

    @Bean(initMethod = "init")
    public Foo foo() {
    	return new Foo();
    }

    @PostConstruct
    public void init() {
    
    }

### 销毁 Bean 的回调

    <bean id="exampleInitBean" class="examples.ExampleBean" destroy-method="cleanup"/>

    public class AnotherExampleBean implements DisposableBean {
    
    	public void destroy() {
    	// do some destruction work (like releasing pooled connections)
    	}
    }

    @Bean(destroyMethod = "cleanup")
    public Bar bar() {
    	return new Bar();
    }

    @PreDestroy
    public void cleanup() {
    
    }

### Bean 的继承

在初始化 Bean 的地方：`RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);`

这里涉及到的就是`<bean parent="" />`中的 parent 属性，我们来看看 Spring 中是用这个来干什么的。

首先，我们要明白，这里的继承和 java 语法中的继承没有任何关系，不过思路是相通的。child bean 会继承 parent bean 的所有配置，也可以覆盖一些配置，当然也可以新增额外的配置。

Spring 中提供了继承自 AbstractBeanDefinition 的 ChildBeanDefinition 来表示 child bean。

看如下一个例子:

    <bean id="inheritedTestBean" abstract="true" class="org.springframework.beans.TestBean">
    	<property name="name" value="parent"/>
    	<property name="age" value="1"/>
    </bean>
    
    <bean id="inheritsWithDifferentClass" class="org.springframework.beans.DerivedTestBean" parent="inheritedTestBean" init-method="initialize">
    	<property name="name" value="override"/>
    </bean>

parent bean 设置了 abstract="true" 所以它不会被实例化，child bean 继承了 parent bean 的两个属性，但是对 name 属性进行了覆写。

child bean 会继承 scope、构造器参数值、属性值、init-method、destroy-method 等等。

当然，我不是说 parent bean 中的 abstract = true 在这里是必须的，只是说如果加上了以后 Spring 在实例化 singleton beans 的时候会忽略这个 bean。

比如下面这个极端 parent bean，它没有指定 class，所以毫无疑问，这个 bean 的作用就是用来充当模板用的 parent bean，此处就必须加上 abstract = true。

    <bean id="inheritedTestBeanWithoutClass" abstract="true">
    	<property name="name" value="parent"/>
    	<property name="age" value="1"/>
    </bean>

### 方法注入

一般来说，我们的应用中大多数的 Bean 都是 singleton 的。singleton 依赖 singleton，或者 prototype 依赖 prototype 都很好解决，直接设置属性依赖就可以了。

但是，如果是 singleton 依赖 prototype 呢？这个时候不能用属性依赖，因为如果用属性依赖的话，我们每次其实拿到的还是第一次初始化时候的 bean。

一种解决方案就是不要用属性依赖，每次获取依赖的 bean 的时候从 BeanFactory 中取。这个也是最常用的方式了吧。怎么取，我就不介绍了，大部分 Spring 项目大家都会定义那么个工具类的。

另一种解决方案就是使用 Lookup method。

### BeanPostProcessor

应该说 BeanPostProcessor 概念在 Spring 中也是比较重要的。我们看下接口定义：

    public interface BeanPostProcessor {
    
    	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;
    
       	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;
    }

看这个接口中的两个方法名字我们大体上可以猜测 bean 在初始化之前会执行 postProcessBeforeInitialization 这个方法，初始化完成之后会执行 postProcessAfterInitialization 这个方法。但是，这么理解是非常片面的。

首先，我们要明白，除了我们自己定义的 BeanPostProcessor 实现外，Spring 容器在启动时自动给我们也加了几个。如在获取 BeanFactory 的 obtainFactory() 方法结束后的 prepareBeanFactory(factory)，大家仔细看会发现，Spring 往容器中添加了这两个 BeanPostProcessor：ApplicationContextAwareProcessor、ApplicationListenerDetector。

我们回到这个接口本身，请看第一个方法，这个方法接受的第一个参数是 bean 实例，第二个参数是 bean 的名字，重点在返回值将会作为新的 bean 实例，所以，没事的话这里不能随便返回个 null。那意味着什么呢？我们很容易想到的就是，我们这里可以对一些我们想要修饰的 bean 实例做一些事情。但是对于 Spring 框架来说，它会决定是不是要在这个方法中返回 bean 实例的代理，这样就有更大的想象空间了。

最后，我们说说如果我们自己定义一个 bean 实现 BeanPostProcessor 的话，它的执行时机是什么时候？

如果仔细看了代码分析的话，其实很容易知道了，在 bean 实例化完成、属性注入完成之后，会执行回调方法，具体请参见类 AbstractAutowireCapableBeanFactory#initBean 方法。

首先会回调几个实现了 Aware 接口的 bean，然后就开始回调 BeanPostProcessor 的 postProcessBeforeInitialization 方法，之后是回调 init-method，然后再回调 BeanPostProcessor 的 postProcessAfterInitialization 方法。

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
