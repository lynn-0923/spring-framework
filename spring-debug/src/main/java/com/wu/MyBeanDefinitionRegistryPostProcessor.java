/**
 * Author:   Lynn
 * Date:     2022/4/11 9:01
 */
package com.wu;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * @author Lynn
 * @create 2022/4/11
 */
public class MyBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(MySelfBeanDefinitionRegistryPostProcessor.class);
		builder.addPropertyValue("name", "wu");
		registry.registerBeanDefinition("wu", builder.getBeanDefinition());

//		registry.registerBeanDefinition("wu",new RootBeanDefinition(MySelfBeanDefinitionRegistryPostProcessor.class));
		System.out.println("postProcessBeanDefinitionRegistry -- MyBeanDefinitionRegistryPostProcessor");
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("postProcessBeanDefinitionRegistry -- MyBeanDefinitionRegistryPostProcessor");
	}
}