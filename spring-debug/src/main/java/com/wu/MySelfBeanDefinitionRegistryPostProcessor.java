/**
 * Author:   Lynn
 * Date:     2022/4/11 9:03
 */
package com.wu;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;

/**
 * @author Lynn
 * @create 2022/4/11
 */
public class MySelfBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MySelfBeanDefinitionRegistryPostProcessor() {
		System.out.println("MySelfBeanDefinitionRegistryPostProcessor init");
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		System.out.println("postProcessBeanDefinitionRegistry -- MySelfBeanDefinitionRegistryPostProcessor");
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("postProcessBeanFactory -- MySelfBeanDefinitionRegistryPostProcessor");
	}
}