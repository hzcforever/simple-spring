package com.example.MySpringBoot.AOP;

public interface PointcutAdvisor extends Advisor {

    Pointcut getPointcut();

}
