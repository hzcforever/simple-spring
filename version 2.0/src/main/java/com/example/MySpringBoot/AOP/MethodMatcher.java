package com.example.MySpringBoot.AOP;

import java.lang.reflect.Method;

public interface MethodMatcher {

    Boolean matchers(Method method, Class beanClass);

}
