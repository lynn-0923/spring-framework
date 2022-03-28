/**
 * Author:   Lynn
 * Date:     2022/3/26 19:54
 */
package com.wu;

import com.wu.test.Person;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Lynn
 * @create 2022/3/26
 */
public class Test {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
//		MyClassPathXmlApplicationContext applicationContext = new MyClassPathXmlApplicationContext("applicationContext.xml");

//		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring-${username}.xml");
		Person person = (Person) applicationContext.getBean("person");
		System.out.println(person);
	}
}