package com.hascode.tutorial.jpa2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PersonTest {
	private static EntityManagerFactory	emf;
	private static EntityManager		em;
	private static EntityTransaction	tx;

	@Before
	public void initEntityManager() throws Exception {
		emf = Persistence.createEntityManagerFactory("hascode-manual");
		em = emf.createEntityManager();
		tx = em.getTransaction();
	}

	@After
	public void closeEntityManager() throws SQLException {
		em.close();
		emf.close();
	}

	@Test
	public void testPersist() {
		tx.begin();
		Person person = new Person();
		person.setNickname("mickey");
		person.setEmail("mickey@testhost.moby");
		em.persist(person);
		tx.commit();

		// look if the entity was persisted and got an id assigned
		assertNotNull(person.getId());

		// lookup entity
		Long id = person.getId();
		Person userFromDatabase = em.find(Person.class, id);
		assertNotNull(userFromDatabase);
		assertEquals("mickey", userFromDatabase.getNickname());
		assertEquals("mickey@testhost.moby", userFromDatabase.getEmail());
	}

	@Test
	public void testEnum() {
		tx.begin();
		Person person = new Person();
		person.setNickname("mickey");
		person.setSex(Sex.FEMALE);
		em.persist(person);
		tx.commit();

		Long id = person.getId();
		Person userFromDatabase = em.find(Person.class, id);

		assertEquals(Sex.FEMALE, userFromDatabase.getSex());
	}

	@Test
	public void testDate() {
		tx.begin();
		final Date date = new Date();
		Person person = new Person();
		person.setNickname("mickey");
		person.setBirthday(date);
		em.persist(person);
		tx.commit();

		Long id = person.getId();
		Person userFromDatabase = em.find(Person.class, id);

		assertEquals(date, userFromDatabase.getBirthday());
	}

	@Test
	public void testHobbies() {
		tx.begin();
		Person person = new Person();
		person.setNickname("mickey");
		final List<String> hobbies = new ArrayList<String>();
		hobbies.add("Coding");
		hobbies.add("Not sleeping");
		hobbies.add("drinking coffee");
		hobbies.add("living the cliché");
		person.setHobbies(hobbies);
		em.persist(person);
		tx.commit();

		Long id = person.getId();
		Person userFromDatabase = em.find(Person.class, id);

		assertEquals(4, userFromDatabase.getHobbies().size());
	}

	@Test
	public void testAddress() {
		tx.begin();

		Address address = new Address();
		address.setCity("Paris");
		address.setStreet("Rue de St. Denis");
		em.persist(address);

		Person person = new Person();
		person.setNickname("mickey");
		person.setAddress(address);
		em.persist(person);
		tx.commit();

		Long id = person.getId();
		Person userFromDatabase = em.find(Person.class, id);

		assertEquals("Paris", userFromDatabase.getAddress().getCity());
	}

	@Test
	public void testBookmarks() {
		tx.begin();

		Bookmark b1 = new Bookmark();
		b1.setTitle("A website");
		b1.setUrl("http://www.hascode.com");

		Bookmark b2 = new Bookmark();
		b2.setTitle("Another website");
		b2.setUrl("http://www.hascode.com/tag/jpa2");
		em.persist(b1);
		em.persist(b2);

		List<Bookmark> bookmarks = new ArrayList<Bookmark>();
		bookmarks.add(b1);
		bookmarks.add(b2);

		Person person = new Person();
		person.setNickname("mickey");
		person.setBookmarks(bookmarks);
		em.persist(person);
		tx.commit();

		Long id = person.getId();
		Person userFromDatabase = em.find(Person.class, id);

		assertEquals("A website", userFromDatabase.getBookmarks().get(0)
				.getTitle());
	}

	@Test
	public void testPet() {
		tx.begin();

		Pet pet1 = new Pet();
		pet1.setName("Nanny the bunny");
		Pet pet2 = new Pet();
		pet2.setName("Doggie Dog");

		List<Pet> pets = new ArrayList<Pet>();
		pets.add(pet1);
		pets.add(pet2);

		Person p1 = new Person();
		p1.setNickname("Mickey");
		p1.setPets(pets);

		Person p2 = new Person();
		p2.setNickname("Minny");
		p2.setPets(pets);

		em.persist(pet1);
		em.persist(pet2);
		em.persist(p1);
		em.persist(p2);
		tx.commit();

		assertNotNull(pet1.getId());

		Person userFromDB = em.find(Person.class, p1.getId());
		assertEquals(2, userFromDB.getPets().size());
	}

	@Test
	public void testJPQL() {
		tx.begin();

		Bookmark b1 = new Bookmark();
		b1.setTitle("Snoring for experts");
		b1.setUrl("http://www.hascode.com");

		em.persist(b1);

		List<Bookmark> bookmarks = new ArrayList<Bookmark>();
		bookmarks.add(b1);

		Person hal = new Person();
		hal.setNickname("HAL9000");
		hal.setBookmarks(bookmarks);
		em.persist(hal);
		em.flush();
		tx.commit();

		// query with named parameters (i prefer this one)
		Query query = em
				.createQuery("SELECT p FROM Person p WHERE p.nickname=:name");
		query.setParameter("name", "HAL9000");
		Person p1 = (Person) query.getResultList().get(0);
		assertEquals(hal.getId(), p1.getId());

		// same with positional parameters
		Query query2 = em
				.createQuery("SELECT p FROM Person p WHERE p.nickname=?1");
		query2.setParameter(1, "HAL9000");
		Person p2 = (Person) query2.getResultList().get(0);
		assertEquals(hal.getId(), p2.getId());

		// an example using joins
		Query query3 = em
				.createQuery("SELECT p FROM Person p LEFT JOIN FETCH p.bookmarks b WHERE b.title=:title");
		query3.setParameter("title", "Snoring for experts");
		Person p3 = (Person) query3.getResultList().get(0);
		assertEquals(hal.getId(), p3.getId());
	}

	@Test
	public void testNamedQueries() {
		tx.begin();
		Person donald = new Person();
		donald.setNickname("Donald");

		Person ronald = new Person();
		ronald.setNickname("Ronald");

		em.persist(donald);
		em.persist(ronald);
		tx.commit();

		List<Person> persons = em.createNamedQuery("findAll").getResultList();
		assertEquals(2, persons.size());

		Query q = em.createNamedQuery("findByNickname");
		q.setParameter("name", "Ronald");
		Person isItRonald = (Person) q.getResultList().get(0);
		assertEquals(ronald.getId(), isItRonald.getId());
	}

	@Test
	public void testCriteriaApi() {
		tx.begin();
		Person donald = new Person();
		donald.setNickname("Donald");

		Person ronald = new Person();
		ronald.setNickname("Ronald");

		em.persist(donald);
		em.persist(ronald);
		tx.commit();

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Person> cq = cb.createQuery(Person.class);
		Root<Person> pr = cq.from(Person.class);
		cq.where(cb.equal(pr.get("nickname"), "Ronald"));
		TypedQuery<Person> typedQuery = em.createQuery(cq);
		List<Person> persons = typedQuery.getResultList();
		assertEquals(ronald.getId(), persons.get(0).getId());

	}
}
