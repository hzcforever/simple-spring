package com.hzc.IOC_AOP.IOC;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * IOC的实现类
 * 1.加载xml配置文件，遍历其中的标签
 * 2.获取标签中的id和class属性，加载class属性对应的类，并创建bean
 * 3.遍历标签中的标签，获取属性值，并将属性值填充到bean中
 * 4.将bean注册到bean容器中
 */

public class SimpleIOC {
    private Map<String, Object> beanMap = new HashMap<>();

    public SimpleIOC(String location) throws Exception {
        loadBeans(location);
    }

    public Object getBean(String name) {
        Object bean = beanMap.get(name);
        if (null == bean) {
            throw new IllegalArgumentException("There is no bean with name:" + name);
        }
        return bean;
    }

    private void registerBean(String id, Object bean) {
        beanMap.put(id, bean);
    }

    private void loadBeans(String location) throws Exception {
        // 加载xml配置文件
        InputStream inputStream = new FileInputStream(location);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(inputStream);
        Element root = doc.getDocumentElement();
        NodeList nodes = root.getChildNodes();

        // 遍历<bean>标签
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                Element ele = (Element) node;
                String id = ele.getAttribute("id");
                String className = ele.getAttribute("class");
                // 加载beanClass
                Class beanClass = null;
                try {
                    beanClass = Class.forName(className);
                } catch (Exception e) {
                    return ;
                }

                // 创建bean
                Object bean = beanClass.newInstance();
                // 遍历<propertiy>标签
                NodeList propertyNodes = ele.getElementsByTagName("property");
                for (int j = 0; j < propertyNodes.getLength(); j++) {
                    Node propertyNode = propertyNodes.item(j);
                    if (propertyNode instanceof Element) {
                        Element propertyElement = (Element) propertyNode;
                        String name = propertyElement.getAttribute("name");
                        String value = propertyElement.getAttribute("value");

                        // 利用反射将bean相关字段访问权限设为可访问
                        Field declaredField = bean.getClass().getDeclaredField(name);
                        declaredField.setAccessible(true);
                        if (value != null && value.length() > 0) {
                            // 将属性值填充到相关字段中
                            declaredField.set(bean, value);
                        } else {
                            String ref = propertyElement.getAttribute("ref");
                            if (null == ref || 0 == ref.length()) {
                                throw new IllegalArgumentException("ref config error");
                            }
                            // 将引用填充到相关字段中
                            declaredField.set(bean, getBean(ref));
                        }
                        // 将bean注册到bean容器中
                        registerBean(id, bean);
                    }
                }
            }
        }
    }

}
