package org.rag4j.meetingplanner.agent.service;

import org.rag4j.meetingplanner.agent.model.Agenda;
import org.rag4j.meetingplanner.agent.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PersonFinder {
    private static final Logger logger = LoggerFactory.getLogger(PersonFinder.class);
    private final Map<String, Person> persons;

    public PersonFinder() {
        persons = new HashMap<>();
        initSampleData();
    }

    public void addPerson(Person person) {
        persons.put(person.email(), person);
    }

    public Person findByEmail(String email) {
        logger.info("Finding person by email: {}", email);
        return persons.get(email);
    }

    public List<Person> getAllPersons() {
        return persons.values().stream().toList();
    }

    private void initSampleData() {
        // Initialize with some sample persons
        addPerson(new Person("jettro@rag4j.org", "Jettro Coenradie", new Agenda()));
        addPerson(new Person("daniel@rag4j.org", "Daniël Spee", new Agenda()));
        addPerson(new Person("joey@rag4j.org", "Joey Visbeen", new Agenda()));
    }
}
