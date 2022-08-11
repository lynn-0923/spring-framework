/**
 * Author:   Lynn
 * Date:     2022/3/27 10:49
 */
package com.wu;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Lynn
 * @create 2022/3/27
 */
public class MyClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {

	public MyClassPathXmlApplicationContext(String configLocation) throws BeansException {
		super(configLocation);
	}

	@Override
	protected void initPropertySources() {
		System.out.println("init propertySource");
	}

	@Override
	protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
//		super.addBeanFactoryPostProcessor(new MyBeanFactoryPostProcessor());
		super.customizeBeanFactory(beanFactory);
	}
}