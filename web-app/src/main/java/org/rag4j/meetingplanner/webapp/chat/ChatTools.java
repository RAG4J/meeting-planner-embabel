package org.rag4j.meetingplanner.webapp.chat;

import com.embabel.agent.api.common.autonomy.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import org.rag4j.meetingplanner.agent.location.model.BookingResult;
import org.rag4j.meetingplanner.agent.location.model.RoomRequest;
import org.rag4j.meetingplanner.agent.meeting.model.MeetingRequest;
import org.rag4j.meetingplanner.agent.meeting.model.MeetingResponse;
import org.rag4j.meetingplanner.agent.meeting.service.MeetingService;
import org.rag4j.meetingplanner.agent.nomnom.model.NomNomOrderRequest;
import org.rag4j.meetingplanner.agent.nomnom.model.NomNomOrderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class ChatTools {
    private static final Logger logger = LoggerFactory.getLogger(ChatTools.class);

    private final AgentPlatform agentPlatform;
    private final MeetingService meetingService;

    public ChatTools(AgentPlatform agentPlatform, MeetingService meetingService) {
        this.agentPlatform = agentPlatform;
        this.meetingService = meetingService;
    }

    @Tool(description = "Book a location for a meeting based on the room request")
    public BookingResult bookLocation(RoomRequest request) {
        logger.info("About to book location with for location type {}, on date {} for {} minutes with {} people",
                request.locationDescription(), request.date(), request.numberOfMinutes(), request.numberOfParticipants());

        var invocation = AgentInvocation.create(this.agentPlatform, BookingResult.class);
        return invocation.invoke(request);
    }

    @Tool(description = "Book a meeting based on the meeting request")
    public MeetingResponse bookMeeting(MeetingRequest request) {
        logger.info("About to book meeting titled '{}' on date {} at {}",
                request.getTitle(), request.getDate(), request.getStartTime());

        var invocation = AgentInvocation.create(this.agentPlatform, MeetingResponse.class);
        var meetingResponse = invocation.invoke(request);
        if (meetingResponse.getMeeting().isPresent()) {
            meetingService.addMeeting(meetingResponse.getMeeting().get());
        }

        return meetingResponse;
    }

    public NomNomOrderResult bookNomNomOrder(NomNomOrderRequest request) {
        logger.info("About to place food and drinks order for location '{}' on date {} with message: {}",
                request.location(), request.deliveryDate(), request.message());

        var invocation = AgentInvocation.create(this.agentPlatform, NomNomOrderResult.class);
        return invocation.invoke(request);
    }
}
