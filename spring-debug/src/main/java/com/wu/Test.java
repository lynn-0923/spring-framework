/**
 * Author:   Lynn
 * Date:     2022/3/26 19:54
 */
package com.wu;

import com.wu.selfEdit.Customer;
import com.wu.selftag.User;
import com.wu.test.Person;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Lynn
 * @create 2022/3/26
 */
public class Test {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext applicationContext = new MyClassPathXmlApplicationContext("applicationContext.xml");
		Person person = (Person) applicationContext.getBean("person");
		System.out.println(person);
//		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("selfedit.xml");
//		Customer customer = applicationContext.getBean(Customer.class);
//		System.out.println(customer);
	}
}