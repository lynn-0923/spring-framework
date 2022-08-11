/**
 * Author:   Lynn
 * Date:     2022/4/10 21:09
 */
package com.wu;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * @author Lynn
 * @create 2022/4/10
 */
public class MyBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("postProcessBeanFactory -- MyBeanFactoryPostProcessor");
	}
}