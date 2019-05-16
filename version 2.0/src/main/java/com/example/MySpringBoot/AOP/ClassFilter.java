package com.example.MySpringBoot.AOP;

public interface ClassFilter {

    Boolean matchers(Class beanClass) throws Exception;

}
