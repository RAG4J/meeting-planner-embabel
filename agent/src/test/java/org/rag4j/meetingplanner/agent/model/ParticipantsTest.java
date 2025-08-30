package org.rag4j.meetingplanner.agent.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantsTest {

    private Participants participants;
    private Person person1;
    private Person person2;
    private Person person3;
    private final LocalDate testDay = LocalDate.of(2024, 6, 10);

    @BeforeEach
    void setUp() {
        // Create persons with empty agendas
        person1 = new Person("john@example.com", "John Doe", new Agenda());
        person2 = new Person("jane@example.com", "Jane Smith", new Agenda());
        person3 = new Person("bob@example.com", "Bob Wilson", new Agenda());
        
        participants = new Participants(List.of(person1, person2, person3));
    }

    @Test
    @DisplayName("All participants are available when no meetings are booked")
    void allParticipantsAvailableWhenNoMeetingsBooked() {
        List<Available> availability = participants.checkAvailabilityFor(
                testDay, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertEquals(3, availability.size());
        assertTrue(availability.stream().allMatch(Available::available));
        assertEquals(person1, availability.get(0).person());
        assertEquals(person2, availability.get(1).person());
        assertEquals(person3, availability.get(2).person());
    }

    @Test
    @DisplayName("Some participants unavailable when they have conflicting meetings")
    void someParticipantsUnavailableWithConflictingMeetings() {
        // Book a meeting for person1 that conflicts with the requested time
        person1.agenda().bookMeeting(testDay, LocalTime.of(9, 30), LocalTime.of(10, 30), "Existing Meeting");

        List<Available> availability = participants.checkAvailabilityFor(
                testDay, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertEquals(3, availability.size());
        assertFalse(availability.get(0).available()); // person1 is not available
        assertTrue(availability.get(1).available());  // person2 is available
        assertTrue(availability.get(2).available());  // person3 is available
    }

    @Test
    @DisplayName("All participants unavailable when all have conflicting meetings")
    void allParticipantsUnavailableWithConflictingMeetings() {
        // Book conflicting meetings for all participants
        person1.agenda().bookMeeting(testDay, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting 1");
        person2.agenda().bookMeeting(testDay, LocalTime.of(10, 30), LocalTime.of(11, 30), "Meeting 2");
        person3.agenda().bookMeeting(testDay, LocalTime.of(9, 30), LocalTime.of(10, 30), "Meeting 3");

        List<Available> availability = participants.checkAvailabilityFor(
                testDay, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertEquals(3, availability.size());
        assertTrue(availability.stream().noneMatch(Available::available));
    }

    @Test
    @DisplayName("Participants available on different day from booked meetings")
    void participantsAvailableOnDifferentDay() {
        // Book meetings for all participants on a different day
        LocalDate differentDay = testDay.plusDays(1);
        person1.agenda().bookMeeting(differentDay, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting 1");
        person2.agenda().bookMeeting(differentDay, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting 2");

        List<Available> availability = participants.checkAvailabilityFor(
                testDay, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertEquals(3, availability.size());
        assertTrue(availability.stream().allMatch(Available::available));
    }

    @Test
    @DisplayName("Successfully books meeting for all available participants")
    void successfullyBooksMeetingForAllParticipants() {
        String meetingTitle = "Team Meeting";
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        List<String> confirmations = participants.bookMeetingForAll(testDay, startTime, endTime, meetingTitle);

        assertEquals(3, confirmations.size());
        assertTrue(confirmations.stream().allMatch(msg -> msg.contains("Meeting booked")));
        assertTrue(confirmations.get(0).contains("John Doe"));
        assertTrue(confirmations.get(1).contains("Jane Smith"));
        assertTrue(confirmations.get(2).contains("Bob Wilson"));

        // Verify meetings are actually booked
        assertFalse(person1.checkAvailability(testDay, startTime, endTime));
        assertFalse(person2.checkAvailability(testDay, startTime, endTime));
        assertFalse(person3.checkAvailability(testDay, startTime, endTime));
    }

    @Test
    @DisplayName("Books meeting even when some participants already have conflicting meetings")
    void booksMeetingEvenWithConflictingMeetings() {
        // Pre-book a meeting for person1 that conflicts
        person1.agenda().bookMeeting(testDay, LocalTime.of(10, 0), LocalTime.of(11, 0), "Existing Meeting");

        String meetingTitle = "Team Meeting";
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        List<String> confirmations = participants.bookMeetingForAll(testDay, startTime, endTime, meetingTitle);

        assertEquals(3, confirmations.size());
        // All participants get booking confirmations regardless of conflicts
        assertTrue(confirmations.stream().allMatch(msg -> msg.contains("Meeting booked")));
    }

    @Test
    @DisplayName("Returns availability for all participants on a given day")
    void returnsAvailabilityForAllParticipantsOnDay() {
        // Book some meetings to create realistic availability patterns
        person1.agenda().bookMeeting(testDay, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting 1");
        person2.agenda().bookMeeting(testDay, LocalTime.of(14, 0), LocalTime.of(15, 0), "Meeting 2");
        // person3 has no meetings

        List<ParticipantAvailability> availability = participants.availabilityForDay(testDay);

        assertEquals(3, availability.size());
        
        // Check person1's availability (should have gaps around 10-11 meeting)
        ParticipantAvailability person1Availability = availability.get(0);
        assertEquals(person1, person1Availability.participant());
        assertTrue(person1Availability.availableSlots().contains("09:00-10:00"));
        assertTrue(person1Availability.availableSlots().contains("11:00-17:00"));
        
        // Check person2's availability (should have gaps around 14-15 meeting)
        ParticipantAvailability person2Availability = availability.get(1);
        assertEquals(person2, person2Availability.participant());
        assertTrue(person2Availability.availableSlots().contains("09:00-14:00"));
        assertTrue(person2Availability.availableSlots().contains("15:00-17:00"));
        
        // Check person3's availability (should be fully available)
        ParticipantAvailability person3Availability = availability.get(2);
        assertEquals(person3, person3Availability.participant());
        assertEquals(List.of("09:00-17:00"), person3Availability.availableSlots());
    }

    @Test
    @DisplayName("Returns empty availability when all participants are fully booked")
    void returnsEmptyAvailabilityWhenFullyBooked() {
        // Book the entire working day for all participants
        person1.agenda().bookMeeting(testDay, LocalTime.of(9, 0), LocalTime.of(17, 0), "All Day Meeting 1");
        person2.agenda().bookMeeting(testDay, LocalTime.of(9, 0), LocalTime.of(17, 0), "All Day Meeting 2");
        person3.agenda().bookMeeting(testDay, LocalTime.of(9, 0), LocalTime.of(17, 0), "All Day Meeting 3");

        List<ParticipantAvailability> availability = participants.availabilityForDay(testDay);

        assertEquals(3, availability.size());
        assertTrue(availability.stream().allMatch(pa -> pa.availableSlots().isEmpty()));
    }

    @Test
    @DisplayName("Handles empty participants list")
    void handlesEmptyParticipantsList() {
        Participants emptyParticipants = new Participants(List.of());

        List<Available> availability = emptyParticipants.checkAvailabilityFor(
                testDay, LocalTime.of(10, 0), LocalTime.of(11, 0));
        List<String> bookingConfirmations = emptyParticipants.bookMeetingForAll(
                testDay, LocalTime.of(10, 0), LocalTime.of(11, 0), "Test Meeting");
        List<ParticipantAvailability> dayAvailability = emptyParticipants.availabilityForDay(testDay);

        assertTrue(availability.isEmpty());
        assertTrue(bookingConfirmations.isEmpty());
        assertTrue(dayAvailability.isEmpty());
    }

    @Test
    @DisplayName("Handles single participant")
    void handlesSingleParticipant() {
        Participants singleParticipant = new Participants(List.of(person1));

        List<Available> availability = singleParticipant.checkAvailabilityFor(
                testDay, LocalTime.of(10, 0), LocalTime.of(11, 0));

        assertEquals(1, availability.size());
        assertEquals(person1, availability.get(0).person());
        assertTrue(availability.get(0).available());
    }

    @Test
    @DisplayName("Participants maintain individual agenda state")
    void participantsMaintainIndividualAgendaState() {
        // Book different meetings for each participant
        person1.agenda().bookMeeting(testDay, LocalTime.of(9, 0), LocalTime.of(10, 0), "Morning Meeting");
        person2.agenda().bookMeeting(testDay, LocalTime.of(14, 0), LocalTime.of(15, 0), "Afternoon Meeting");

        // Check that each person's agenda is independent
        assertTrue(person1.checkAvailability(testDay, LocalTime.of(14, 0), LocalTime.of(15, 0))); // Available for afternoon
        assertFalse(person1.checkAvailability(testDay, LocalTime.of(9, 0), LocalTime.of(10, 0))); // Not available for morning

        assertTrue(person2.checkAvailability(testDay, LocalTime.of(9, 0), LocalTime.of(10, 0))); // Available for morning
        assertFalse(person2.checkAvailability(testDay, LocalTime.of(14, 0), LocalTime.of(15, 0))); // Not available for afternoon

        assertTrue(person3.checkAvailability(testDay, LocalTime.of(9, 0), LocalTime.of(10, 0))); // Fully available
        assertTrue(person3.checkAvailability(testDay, LocalTime.of(14, 0), LocalTime.of(15, 0)));
    }

    @Test
    @DisplayName("Booking confirmation messages contain correct participant information")
    void bookingConfirmationMessagesContainCorrectInfo() {
        String meetingTitle = "Important Meeting";
        LocalTime startTime = LocalTime.of(15, 30);
        LocalTime endTime = LocalTime.of(16, 30);

        List<String> confirmations = participants.bookMeetingForAll(testDay, startTime, endTime, meetingTitle);

        assertEquals(3, confirmations.size());
        assertTrue(confirmations.get(0).contains("John Doe"));
        assertTrue(confirmations.get(0).contains(meetingTitle));
        assertTrue(confirmations.get(0).contains(testDay.toString()));
        
        assertTrue(confirmations.get(1).contains("Jane Smith"));
        assertTrue(confirmations.get(2).contains("Bob Wilson"));
    }
}
