package com.example.MySpringBoot.IOC;

import com.example.MySpringBoot.IOC.XML.XmlBeanFactory;
import com.example.MySpringBoot.Model.Car;
import com.example.MySpringBoot.Model.Wheel;
import org.junit.Test;

public class XmlBeanFactoryTest {

    @Test
    public void getBean() throws Exception {
        String location = getClass().getClassLoader().getResource("simple-spring-ioc.xml").getFile();
        XmlBeanFactory bf = new XmlBeanFactory(location);
        Wheel wheel = (Wheel) bf.getBean("wheel");
        System.out.println(wheel);
        Car car = (Car) bf.getBean("car");
        System.out.println(car.getWidth() + "m宽， " + car.getMoney());
    }
}
