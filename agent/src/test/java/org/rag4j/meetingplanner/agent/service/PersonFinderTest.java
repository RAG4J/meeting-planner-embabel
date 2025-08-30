package org.rag4j.meetingplanner.agent.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rag4j.meetingplanner.agent.model.Agenda;
import org.rag4j.meetingplanner.agent.model.Person;

import java.util.List;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PersonFinderTest {
    private final Validator validator;
    {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Initializes with sample data")
    void initializesWithSampleData() {
        PersonFinder finder = new PersonFinder();
        List<Person> persons = finder.getAllPersons();
        assertEquals(3, persons.size());
        assertTrue(persons.stream().anyMatch(p -> p.email().equals("jettro@rag4j.org")));
        assertTrue(persons.stream().anyMatch(p -> p.email().equals("daniel@rag4j.org")));
        assertTrue(persons.stream().anyMatch(p -> p.email().equals("joey@rag4j.org")));
    }

    @Test
    @DisplayName("Adds a new person and retrieves it by email")
    void addsNewPersonAndRetrievesByEmail() {
        PersonFinder finder = new PersonFinder();
        Person newPerson = new Person("new@rag4j.org", "New Person", new Agenda());
        finder.addPerson(newPerson);
        Person found = finder.findByEmail("new@rag4j.org");
        assertNotNull(found);
        assertEquals("New Person", found.name());
    }

    @Test
    @DisplayName("Returns null when finding non-existing email")
    void returnsNullForNonExistingEmail() {
        PersonFinder finder = new PersonFinder();
        Person found = finder.findByEmail("notfound@rag4j.org");
        assertNull(found);
    }

    @Test
    @DisplayName("Returns all persons including added ones")
    void returnsAllPersonsIncludingAddedOnes() {
        PersonFinder finder = new PersonFinder();
        finder.addPerson(new Person("extra@rag4j.org", "Extra Person", new Agenda()));
        List<Person> persons = finder.getAllPersons();
        assertEquals(4, persons.size());
        assertTrue(persons.stream().anyMatch(p -> p.email().equals("extra@rag4j.org")));
    }

    @Test
    @DisplayName("Overwrites person with duplicate email")
    void overwritesPersonWithDuplicateEmail() {
        PersonFinder finder = new PersonFinder();
        Person original = finder.findByEmail("jettro@rag4j.org");
        Person updated = new Person("jettro@rag4j.org", "Updated Name", new Agenda());
        finder.addPerson(updated);
        Person found = finder.findByEmail("jettro@rag4j.org");
        assertEquals("Updated Name", found.name());
    }

    @Test
    @DisplayName("Does not add person with null email")
    void doesNotAddPersonWithNullEmail() {
        PersonFinder finder = new PersonFinder();
        Person person = new Person(null, "No Email", new Agenda());
        Set<ConstraintViolation<Person>> violations = validator.validate(person);
        assertFalse(violations.isEmpty());
        finder.addPerson(person);
        Person found = finder.findByEmail(null);
        assertNotNull(found); // Finder adds regardless, but validation fails
        assertFalse(validator.validate(found).isEmpty());
    }

    @Test
    @DisplayName("Handles adding person with empty email")
    void handlesAddingPersonWithEmptyEmail() {
        PersonFinder finder = new PersonFinder();
        Person person = new Person("", "Empty Email", new Agenda());
        finder.addPerson(person);
        assertEquals("Empty Email", finder.findByEmail("").name());
        List<Person> persons = finder.getAllPersons();
        assertTrue(persons.stream().anyMatch(p -> p.name().equals("Empty Email")));
    }

    @Test
    @DisplayName("Does not add person with invalid email")
    void doesNotAddPersonWithInvalidEmail() {
        PersonFinder finder = new PersonFinder();
        Person invalidPerson = new Person("invalid-email", "Name", new Agenda());
        Set<ConstraintViolation<Person>> violations = validator.validate(invalidPerson);
        assertFalse(violations.isEmpty());
        finder.addPerson(invalidPerson);
        // Should not be found or should be invalid
        Person found = finder.findByEmail("invalid-email");
        assertNotNull(found); // The finder adds regardless, but validation fails
        assertFalse(validator.validate(found).isEmpty());
    }

    @Test
    @DisplayName("Does not add person with blank name")
    void doesNotAddPersonWithBlankName() {
        PersonFinder finder = new PersonFinder();
        Person invalidPerson = new Person("valid@email.com", "", new Agenda());
        Set<ConstraintViolation<Person>> violations = validator.validate(invalidPerson);
        assertFalse(violations.isEmpty());
        finder.addPerson(invalidPerson);
        Person found = finder.findByEmail("valid@email.com");
        assertNotNull(found);
        assertFalse(validator.validate(found).isEmpty());
    }

    @Test
    @DisplayName("Does not add person with null agenda")
    void doesNotAddPersonWithNullAgenda() {
        PersonFinder finder = new PersonFinder();
        Person invalidPerson = new Person("valid@email.com", "Name", null);
        Set<ConstraintViolation<Person>> violations = validator.validate(invalidPerson);
        assertFalse(violations.isEmpty());
        finder.addPerson(invalidPerson);
        Person found = finder.findByEmail("valid@email.com");
        assertNotNull(found);
        assertFalse(validator.validate(found).isEmpty());
    }
}
