package com.spring.demo;

import com.spring.demo.bean.Person;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author wy
 * @company 妈妈好网络科技
 * @Classname ProjectRunTest
 * @Description TODO
 * @Date 2020/3/10 8:46 下午
 */

public class ProjectRunTest {


	public static void main(String[] args) {

		ApplicationContext applicationContext = new FileSystemXmlApplicationContext("//Users/wy/project/spring-framework-5.2.0.RELEASE/spring-demo/src/main/resources/beans.xml");
		Person person = (Person) applicationContext.getBean("person");
		System.out.println(person.getAge() + "----" + person.getName());
	}
}
