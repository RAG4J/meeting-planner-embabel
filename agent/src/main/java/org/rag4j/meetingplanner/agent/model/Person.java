package org.rag4j.meetingplanner.agent.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record Person(String email, String name, Agenda agenda) {
    private static final Logger logger = LoggerFactory.getLogger(Person.class);

    @Tool
    public boolean checkAvailability(LocalDate day, LocalTime start, LocalTime end) {
        logger.info("Checking availability of person {} for {} from {} to {}", email, day, start, end);
        return agenda.checkAvailability(day, start, end);
    }

    @Tool
    public void bookMeeting(LocalDate day, LocalTime start, LocalTime end, String title) {
        logger.info("Book meeting with person {} for {} from {} to {}", email, day, start, end);
        agenda.bookMeeting(day, start, end, title);
    }

    @Tool
    public List<String> availabilityForDay(LocalDate day) {
        logger.info("Getting availability of person {} for {}", email, day);
        return agenda.availabilityForDay(day);
    }
}
