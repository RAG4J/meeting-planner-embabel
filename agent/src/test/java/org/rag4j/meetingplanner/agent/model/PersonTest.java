package org.rag4j.meetingplanner.agent.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rag4j.meetingplanner.common.model.Agenda;
import org.rag4j.meetingplanner.agent.meeting.model.Person;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PersonTest {
    private final Validator validator;
    {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Returns full working hours when no meetings are booked for person")
    void returnsFullDayWhenNoMeetingsBookedForPerson() {
        Person person = new Person("john@example.com", "John Doe", new Agenda());
        List<String> slots = person.availabilityForDay(LocalDate.of(2024, 6, 10));
        assertEquals(List.of("09:00-17:00"), slots);
    }

    @Test
    @DisplayName("Returns available slots for person with multiple non-overlapping meetings")
    void returnsAvailableSlotsForPersonWithNonOverlappingMeetings() {
        Agenda agenda = new Agenda();
        LocalDate day = LocalDate.of(2024, 6, 10);
        agenda.bookMeeting(day, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting 1");
        agenda.bookMeeting(day, LocalTime.of(13, 0), LocalTime.of(14, 0), "Meeting 2");
        Person person = new Person("jane@example.com", "Jane Doe", agenda);
        List<String> slots = person.availabilityForDay(day);
        assertEquals(List.of("09:00-10:00", "11:00-13:00", "14:00-17:00"), slots);
    }

    @Test
    @DisplayName("Returns no slots when person's meeting covers entire working hours")
    void returnsNoSlotsWhenPersonMeetingCoversWholeDay() {
        Agenda agenda = new Agenda();
        LocalDate day = LocalDate.of(2024, 6, 10);
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(17, 0), "All Day Meeting");
        Person person = new Person("alex@example.com", "Alex Smith", agenda);
        List<String> slots = person.availabilityForDay(day);
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Returns full working hours for person on day with no meetings but meetings on other days")
    void returnsFullDayForPersonWhenNoMeetingsOnGivenDay() {
        Agenda agenda = new Agenda();
        LocalDate day = LocalDate.of(2024, 6, 10);
        agenda.bookMeeting(day.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), "Other Day Meeting");
        Person person = new Person("lisa@example.com", "Lisa Ray", agenda);
        List<String> slots = person.availabilityForDay(day);
        assertEquals(List.of("09:00-17:00"), slots);
    }

    @Test
    @DisplayName("Returns available slots for person with adjacent meetings")
    void returnsAvailableSlotsForPersonWithAdjacentMeetings() {
        Agenda agenda = new Agenda();
        LocalDate day = LocalDate.of(2024, 6, 10);
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting 1");
        agenda.bookMeeting(day, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting 2");
        Person person = new Person("sam@example.com", "Sam Lee", agenda);
        List<String> slots = person.availabilityForDay(day);
        assertEquals(List.of("11:00-17:00"), slots);
    }

    @Test
    @DisplayName("Fails validation for blank email")
    void failsValidationForBlankEmail() {
        Person person = new Person("", "John Doe", new Agenda());
        Set<ConstraintViolation<Person>> violations = validator.validate(person);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Fails validation for invalid email format")
    void failsValidationForInvalidEmailFormat() {
        Person person = new Person("not-an-email", "John Doe", new Agenda());
        Set<ConstraintViolation<Person>> violations = validator.validate(person);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Fails validation for blank name")
    void failsValidationForBlankName() {
        Person person = new Person("john@example.com", "", new Agenda());
        Set<ConstraintViolation<Person>> violations = validator.validate(person);
        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Fails validation for null agenda")
    void failsValidationForNullAgenda() {
        Person person = new Person("john@example.com", "John Doe", null);
        Set<ConstraintViolation<Person>> violations = validator.validate(person);
        assertFalse(violations.isEmpty());
    }

}
