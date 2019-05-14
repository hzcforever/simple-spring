package com.example.MySpringBoot.IOC.XML;

import com.example.MySpringBoot.IOC.BeanDefinition;
import com.example.MySpringBoot.IOC.BeanPostProcessor;
import com.example.MySpringBoot.IOC.BeanReference;
import com.example.MySpringBoot.IOC.Factory.BeanFactory;
import com.example.MySpringBoot.IOC.Factory.BeanFactoryAware;
import com.example.MySpringBoot.IOC.PropertyValue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlBeanFactory implements BeanFactory {

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private List<String> beanDefinitionNames = new ArrayList<>();

    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    private XmlBeanDefinitionReader beanDefinitionReader;

    public XmlBeanFactory(String location) throws Exception {
        beanDefinitionReader = new XmlBeanDefinitionReader();
        loadBeanDefinitions(location);
    }

    public void loadBeanDefinitions(String location) throws Exception {
        beanDefinitionReader.loadBeanDefinitions(location);
        registerBeanDefinition();
        registerBeanPostProcessor();
    }

    @Override
    public Object getBean(String name) throws Exception {
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        if (null == beanDefinition) {
            throw new IllegalArgumentException("no this bean with name " + name);
        }
        Object bean = beanDefinition.getBean();
        if (null == bean) {
            bean = createBean(beanDefinition);
            bean = initializeBean(bean, name);
            beanDefinition.setBean(bean);
        }
        return bean;
    }

    private Object createBean(BeanDefinition bd) throws Exception {
        Object bean = bd.getBeanClass().newInstance();
        applyPropertyValues(bean, bd);
        return bean;
    }

    private void applyPropertyValues(Object bean, BeanDefinition bd) throws Exception {
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(this);
        }
        for (PropertyValue propertyValue : bd.getPropertyValues().getPropertyValues()) {
            Object value = propertyValue.getValue();
            if (value instanceof BeanReference) {
                BeanReference beanReference = (BeanReference) value;
                value = getBean(beanReference.getName());
            }
            try {
                Method declaredMethod = bean.getClass().getDeclaredMethod("set" +
                        propertyValue.getName().substring(0, 1).toUpperCase()
                        + propertyValue.getName().substring(1), value.getClass());

                declaredMethod.setAccessible(true);
                declaredMethod.invoke(bean, value);

            } catch (NoSuchMethodException e) {
                Field declaredField = bean.getClass().getDeclaredField(propertyValue.getName());
                declaredField.setAccessible(true);
                try {
                    int val = Integer.valueOf((String) value);
                    declaredField.set(bean, val);
                } catch (Exception error) {
                    declaredField.set(bean, value);
                }

            }
        }
    }

    private Object initializeBean(Object bean, String name) throws Exception {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.postProcessBeforeInitialization(bean, name);
        }
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.postProcessAfterInitialization(bean, name);
        }
        return bean;
    }

    private void registerBeanDefinition() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionReader.getRegistry().entrySet()) {
            String name = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            beanDefinitionMap.put(name, beanDefinition);
            beanDefinitionNames.add(name);
        }
    }

    private void registerBeanPostProcessor() throws Exception {
        List beans = getBeansForType(BeanPostProcessor.class);
        for (Object bean : beans) {
            addBeanPostProcessor((BeanPostProcessor) bean);
        }
    }

    public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        beanPostProcessors.add(beanPostProcessor);
    }

    public List getBeansForType(Class type) throws Exception {
        List beans = new ArrayList<>();
        for (String beanDefinitionName : beanDefinitionNames) {
            if (type.isAssignableFrom(beanDefinitionMap.get(beanDefinitionName).getBeanClass())) {
                beans.add(getBean(beanDefinitionName));
            }
        }
        return beans;
    }
}
