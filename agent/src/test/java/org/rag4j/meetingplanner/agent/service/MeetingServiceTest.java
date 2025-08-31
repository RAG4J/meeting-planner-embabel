package org.rag4j.meetingplanner.agent.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.rag4j.meetingplanner.agent.model.meeting.Meeting;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeetingServiceTest {

    @Test
    @DisplayName("Returns empty list when no meetings are added")
    void returnsEmptyListWhenNoMeetingsAdded() {
        MeetingService service = new MeetingService();
        List<Meeting> meetings = service.getAllMeetings();
        assertTrue(meetings.isEmpty());
    }

    @Test
    @DisplayName("Returns single meeting after adding one")
    void returnsSingleMeetingAfterAddingOne() {
        MeetingService service = new MeetingService();
        Meeting meeting = new Meeting("Title", "Desc", LocalDateTime.of(2025, 8, 30, 10, 0), 60);
        service.addMeeting(meeting);
        List<Meeting> meetings = service.getAllMeetings();
        assertEquals(1, meetings.size());
        assertEquals(meeting, meetings.getFirst());
    }

    @Test
    @DisplayName("Returns meetings sorted by start time")
    void returnsMeetingsSortedByStartTime() {
        MeetingService service = new MeetingService();
        Meeting m1 = new Meeting("Early", "", LocalDateTime.of(2025, 8, 30, 9, 0), 30);
        Meeting m2 = new Meeting("Late", "", LocalDateTime.of(2025, 8, 30, 15, 0), 30);
        Meeting m3 = new Meeting("Middle", "", LocalDateTime.of(2025, 8, 30, 12, 0), 30);
        service.addMeeting(m2);
        service.addMeeting(m1);
        service.addMeeting(m3);
        List<Meeting> meetings = service.getAllMeetings();
        assertEquals(List.of(m1, m3, m2), meetings);
    }

    @Test
    @DisplayName("Handles meetings with identical start times")
    void handlesMeetingsWithIdenticalStartTimes() {
        MeetingService service = new MeetingService();
        LocalDateTime start = LocalDateTime.of(2025, 8, 30, 10, 0);
        Meeting m1 = new Meeting("A", "", start, 30);
        Meeting m2 = new Meeting("B", "", start, 45);
        service.addMeeting(m1);
        service.addMeeting(m2);
        List<Meeting> meetings = service.getAllMeetings();
        assertEquals(2, meetings.size());
        assertTrue(meetings.contains(m1));
        assertTrue(meetings.contains(m2));
    }

    @Test
    @DisplayName("Allows adding meeting with null description and location")
    void allowsAddingMeetingWithNullFields() {
        MeetingService service = new MeetingService();
        Meeting meeting = new Meeting("Title", null, LocalDateTime.of(2025, 8, 30, 11, 0), 60);
        meeting.setLocation(null);
        service.addMeeting(meeting);
        List<Meeting> meetings = service.getAllMeetings();
        assertEquals(1, meetings.size());
        assertNull(meetings.getFirst().getDescription());
        assertNull(meetings.getFirst().getLocation());
    }
}

