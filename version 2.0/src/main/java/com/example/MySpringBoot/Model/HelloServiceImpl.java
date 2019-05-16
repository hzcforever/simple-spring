package com.example.MySpringBoot.Model;

public class HelloServiceImpl implements HelloService {

    @Override
    public void sayHelloWorld() {
        System.out.println("hello world!");
    }
}
