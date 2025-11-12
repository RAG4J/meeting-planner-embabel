package org.rag4j.meetingplanner.agent.meeting.service;

import org.rag4j.meetingplanner.agent.meeting.model.Meeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class MeetingService {
    private static final Logger logger = LoggerFactory.getLogger(MeetingService.class);

    private final List<Meeting> meetings;

    public MeetingService() {
        this.meetings = new java.util.ArrayList<>();
    }

    public List<Meeting> getAllMeetings() {
        return meetings.stream()
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .toList();    }

    public void addMeeting(Meeting meeting) {
        logger.info("Adding meeting {}, for participants {} on {}",
                meeting.getTitle(), String.join(",", meeting.getParticipants()), meeting.getStartTime());
        meetings.add(meeting);
    }
}
