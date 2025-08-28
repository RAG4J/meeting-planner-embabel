package org.rag4j.meetingplanner.agent.service;

import org.rag4j.meetingplanner.agent.model.Meeting;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetingService {
    private final List<Meeting> meetings;

    public MeetingService() {
        this.meetings = new java.util.ArrayList<>();
    }

    public List<Meeting> getAllMeetings() {
        return meetings;
    }

    public void addMeeting(Meeting meeting) {
        meetings.add(meeting);
    }
}
