package com.hzc.IOC_AOP.IOC;

public class SimpleIOCTest {

    public static void main(String[] args) throws Exception {
        String location = SimpleIOC.class.getClassLoader().getResource("com/hzc/IOC_AOP/IOC/ioc.xml").getFile();
        SimpleIOC bf = new SimpleIOC(location);
        Car car = (Car) bf.getBean("car");
        System.out.println(car + " | " + car.getName());
        Wheel wheel = (Wheel) bf.getBean("wheel");
        System.out.println(wheel + " | " + wheel.getBrand());
    }
}
