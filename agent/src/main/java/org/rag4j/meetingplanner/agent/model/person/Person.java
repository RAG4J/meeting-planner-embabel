package org.rag4j.meetingplanner.agent.model.person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record Person(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String name,
        @NotNull
        Agenda agenda
) {
    private static final Logger logger = LoggerFactory.getLogger(Person.class);

    public boolean checkAvailability(LocalDate day, LocalTime start, LocalTime end) {
        var available = agenda.checkAvailability(day, start, end);
        logger.info("Person {} is {}available on {} from {} to {}", email, available ? "" : "not ", day, start, end);
        return available;
    }

    public String bookMeeting(LocalDate day, LocalTime start, LocalTime end, String title) {
        logger.info("Book meeting with person {} for {} from {} to {}", email, day, start, end);
        agenda.bookMeeting(day, start, end, title);
        return "Meeting booked for " + name + " on " + day + " from " + start + " to " + end + " titled '" + title + "'";
    }

    public List<String> availabilityForDay(LocalDate day) {
        logger.info("Getting availability of person {} for {}", email, day);
        return agenda.availabilityForDay(day);
    }
}
