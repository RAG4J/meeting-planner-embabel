package org.rag4j.meetingplanner.agent.service;

import org.rag4j.meetingplanner.agent.model.Agenda;
import org.rag4j.meetingplanner.agent.model.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
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

    @Tool(description = "Find a person by their email address")
    public Person findByEmail(String email) {
        logger.info("Finding person by email: {}", email);
        return persons.get(email);
    }

    @Tool(description = "Find the availability of a person, true if the person is available")
    public boolean checkPersonAvailability(String email, LocalDate date, LocalTime startTime, LocalTime endTime) {
        logger.info("Checking availability of person: {}", email);
        Person person = findByEmail(email);
        return person.checkAvailability(date, startTime, endTime);
    }

    @Tool(description = "Book a meeting for a person")
    public String bookMeetingForPerson(String email, LocalDate date, LocalTime startTime, LocalTime endTime, String title) {
        logger.info("Booking meeting for person: {}", email);
        Person person = findByEmail(email);
        return person.bookMeeting(date, startTime, endTime, title);
    }

    @Tool(description = "Get the availability of a person for a given day")
    public String getPersonAvailabilityForDay(String email, LocalDate date) {
        logger.info("Getting availability for person: {} on date: {}", email, date);
        Person person = findByEmail(email);
        return String.join(", ", person.availabilityForDay(date));
    }

    private void initSampleData() {
        // Initialize with some sample persons
        addPerson(new Person("jettro@rag4j.org", "Jettro Coenradie", new Agenda()));
        addPerson(new Person("daniel@rag4j.org", "Daniël Spee", new Agenda()));
    }
}
