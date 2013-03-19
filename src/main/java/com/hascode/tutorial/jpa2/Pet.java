package com.hascode.tutorial.jpa2;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
public class Pet {
	@Id
	@GeneratedValue
	private Long			id;

	private String			name;

	@ManyToMany(mappedBy = "pets")
	private List<Person>	owner;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<Person> getOwner() {
		return owner;
	}

	public void setOwner(List<Person> owner) {
		this.owner = owner;
	}
}
