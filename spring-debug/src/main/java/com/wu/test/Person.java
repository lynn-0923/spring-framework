package com.wu.test;

public class Person {

	private Integer id;
	private String name;

	public Person() {
		System.out.println("construct init");
	}

	public Person(String name, Integer id) {
		this.id = id;
		this.name = name;
	}

	public Person(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Person{" +
				"id=" + id +
				", name='" + name + '\'' +
				'}';
	}
}
