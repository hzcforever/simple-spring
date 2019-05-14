package com.example.MySpringBoot.IOC.Factory;

public interface BeanFactory {

    Object getBean(String beanId) throws Exception;

}
