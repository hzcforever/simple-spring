package com.example.MySpringBoot.AOP;

import com.example.MySpringBoot.Model.HelloService;
import com.example.MySpringBoot.Model.HelloServiceImpl;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AspectJExpressionPointcutTest {

    @Test
    public void testClassFilter() throws Exception {
        String expression = "execution(* com.example.MySpringBoot.*.*(..))";
        AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();
        aspectJExpressionPointcut.setExpression(expression);
        boolean matches = aspectJExpressionPointcut.matchers(HelloService.class);
        assertTrue(matches);
    }

    @Test
    public void testMethodMatcher() throws Exception {
        String expression = "execution(* com.example.MySpringBoot.Model.*.sayHelloWorld(..))";
        AspectJExpressionPointcut aspectJExpressionPointcut = new AspectJExpressionPointcut();
        aspectJExpressionPointcut.setExpression(expression);
        boolean matches = aspectJExpressionPointcut.matchers(
                HelloServiceImpl.class.getDeclaredMethod("sayHelloWorld"), HelloServiceImpl.class);
        assertTrue(matches);
    }
}
