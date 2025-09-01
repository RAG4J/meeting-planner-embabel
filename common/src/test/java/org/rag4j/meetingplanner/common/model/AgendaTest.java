package org.rag4j.meetingplanner.common.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgendaTest {

    private Agenda agenda;
    private final LocalDate day = LocalDate.of(2024, 6, 10);

    @BeforeEach
    void setUp() {
        agenda = new Agenda();
    }

    @Test
    @DisplayName("Availability is true when agenda is empty")
    void availabilityTrueWhenAgendaIsEmpty() {
        assertTrue(agenda.checkAvailability(day, LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("Booking a meeting blocks the same time slot")
    void bookingBlocksSameTimeSlot() {
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting");
        assertFalse(agenda.checkAvailability(day, LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("Availability is false when new meeting overlaps with existing")
    void availabilityFalseOnOverlap() {
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting");
        assertFalse(agenda.checkAvailability(day, LocalTime.of(9, 30), LocalTime.of(10, 30)));
    }

    @Test
    @DisplayName("Availability is true when new meeting is before existing")
    void availabilityTrueWhenBeforeExisting() {
        agenda.bookMeeting(day, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting");
        assertTrue(agenda.checkAvailability(day, LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("Availability is true when new meeting is after existing")
    void availabilityTrueWhenAfterExisting() {
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting");
        assertTrue(agenda.checkAvailability(day, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    @DisplayName("Availability is true for different day")
    void availabilityTrueForDifferentDay() {
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting");
        assertTrue(agenda.checkAvailability(day.plusDays(1), LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("Booking multiple meetings with no overlap is allowed")
    void bookingMultipleNonOverlappingMeetings() {
        agenda.bookMeeting(day, LocalTime.of(8, 0), LocalTime.of(9, 0), "Meeting 1");
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting 2");
        assertTrue(agenda.checkAvailability(day, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    @DisplayName("Availability is false when new meeting starts before and ends after existing meeting")
    void availabilityFalseWhenEnclosingExistingMeeting() {
        agenda.bookMeeting(day, LocalTime.of(9, 30), LocalTime.of(10, 0), "Meeting");
        assertFalse(agenda.checkAvailability(day, LocalTime.of(9, 0), LocalTime.of(10, 30)));
    }

    @Test
    @DisplayName("Availability is true when new meeting ends exactly at existing meeting start")
    void availabilityTrueWhenEndsAtExistingStart() {
        agenda.bookMeeting(day, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting");
        assertTrue(agenda.checkAvailability(day, LocalTime.of(9, 0), LocalTime.of(10, 0)));
    }

    @Test
    @DisplayName("Availability is true when new meeting starts exactly at existing meeting end")
    void availabilityTrueWhenStartsAtExistingEnd() {
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting");
        assertTrue(agenda.checkAvailability(day, LocalTime.of(10, 0), LocalTime.of(11, 0)));
    }

    @Test
    @DisplayName("Returns full working hours when no meetings are booked")
    void returnsFullDayWhenNoMeetingsBooked() {
        List<String> slots = agenda.availabilityForDay(day);
        assertEquals(List.of("09:00-17:00"), slots);
    }

    @Test
    @DisplayName("Returns available slots between two meetings")
    void returnsSlotsBetweenMeetings() {
        agenda.bookMeeting(day, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting 1");
        agenda.bookMeeting(day, LocalTime.of(13, 0), LocalTime.of(14, 0), "Meeting 2");
        List<String> slots = agenda.availabilityForDay(day);
        assertEquals(List.of("09:00-10:00", "11:00-13:00", "14:00-17:00"), slots);
    }

    @Test
    @DisplayName("Returns no slots when meeting covers entire working hours")
    void returnsNoSlotsWhenMeetingCoversWholeDay() {
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(17, 0), "All Day Meeting");
        List<String> slots = agenda.availabilityForDay(day);
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Ignores meetings outside working hours")
    void ignoresMeetingsOutsideWorkingHours() {
        agenda.bookMeeting(day, LocalTime.of(7, 0), LocalTime.of(8, 0), "Early Meeting");
        agenda.bookMeeting(day, LocalTime.of(18, 0), LocalTime.of(19, 0), "Late Meeting");
        List<String> slots = agenda.availabilityForDay(day);
        assertEquals(List.of("09:00-17:00"), slots);
    }

    @Test
    @DisplayName("Clamps meeting that starts before working hours")
    void clampsMeetingStartingBeforeWorkingHours() {
        agenda.bookMeeting(day, LocalTime.of(8, 0), LocalTime.of(10, 0), "Early Meeting");
        List<String> slots = agenda.availabilityForDay(day);
        assertEquals(List.of("10:00-17:00"), slots);
    }

    @Test
    @DisplayName("Clamps meeting that ends after working hours")
    void clampsMeetingEndingAfterWorkingHours() {
        agenda.bookMeeting(day, LocalTime.of(16, 0), LocalTime.of(18, 0), "Late Meeting");
        List<String> slots = agenda.availabilityForDay(day);
        assertEquals(List.of("09:00-16:00"), slots);
    }

    @Test
    @DisplayName("Returns available slots when meetings are adjacent")
    void returnsSlotsWhenMeetingsAreAdjacent() {
        agenda.bookMeeting(day, LocalTime.of(9, 0), LocalTime.of(10, 0), "Meeting 1");
        agenda.bookMeeting(day, LocalTime.of(10, 0), LocalTime.of(11, 0), "Meeting 2");
        List<String> slots = agenda.availabilityForDay(day);
        assertEquals(List.of("11:00-17:00"), slots);
    }

    @Test
    @DisplayName("Returns available slots for day with overlapping meetings")
    void returnsSlotsForOverlappingMeetings() {
        agenda.bookMeeting(day, LocalTime.of(10, 0), LocalTime.of(12, 0), "Meeting 1");
        agenda.bookMeeting(day, LocalTime.of(11, 0), LocalTime.of(13, 0), "Meeting 2");
        List<String> slots = agenda.availabilityForDay(day);
        assertEquals(List.of("09:00-10:00", "13:00-17:00"), slots);
    }

    @Test
    @DisplayName("Returns full working hours for a day with no meetings but meetings on other days")
    void returnsFullDayWhenNoMeetingsOnGivenDay() {
        agenda.bookMeeting(day.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), "Other Day Meeting");
        List<String> slots = agenda.availabilityForDay(day);
        assertEquals(List.of("09:00-17:00"), slots);
    }
}
