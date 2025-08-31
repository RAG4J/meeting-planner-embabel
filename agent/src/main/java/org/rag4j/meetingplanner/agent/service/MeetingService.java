package org.rag4j.meetingplanner.agent.service;

import org.rag4j.meetingplanner.agent.model.meeting.Meeting;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MeetingService {
    private final List<Meeting> meetings;

    public MeetingService() {
        this.meetings = new java.util.ArrayList<>();
    }

    public List<Meeting> getAllMeetings() {
        return meetings.stream()
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .toList();    }

    public void addMeeting(Meeting meeting) {
        meetings.add(meeting);
    }
}
