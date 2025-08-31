package org.rag4j.meetingplanner.agent.model.person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;

import java.time.LocalDate;
import java.util.List;

/**
 * Represents a group of participants for a meeting.
 *
 * @param participants list of participants
 */
public record Participants(List<Person> participants) {
    private static final Logger logger = LoggerFactory.getLogger(Participants.class);

    @Tool(description = "Checks the availability of all participants for a given date and time range")
    public List<Available> checkAvailabilityFor(LocalDate day, java.time.LocalTime start, java.time.LocalTime end) {
        logger.info("Checking availability for all participants on {} from {} to {}", day, start, end);
        return participants.stream()
                .map(person -> new Available(person, person.checkAvailability(day, start, end)))
                .toList();
    }

    @Tool(description = "Book a meeting for all participants. Returns a list of confirmation messages from each participant.")
    public List<String> bookMeetingForAll(LocalDate day, java.time.LocalTime start, java.time.LocalTime end, String title) {
        logger.info("Booking meeting for all participants on {} from {} to {} titled '{}'", day, start, end, title);
        return participants.stream()
                .map(person -> person.bookMeeting(day, start, end, title))
                .toList();
    }

    @Tool (description = "Get the availability of all participants for a given day. Returns a list of available time slots for each participant.")
    public List<AvailabilityOfPerson> availabilityForDay(LocalDate day) {
        logger.info("Getting availability for all participants on {}", day);
        return participants.stream()
                .map(person -> new AvailabilityOfPerson(person.availabilityForDay(day), person))
                .toList();
    }
}
