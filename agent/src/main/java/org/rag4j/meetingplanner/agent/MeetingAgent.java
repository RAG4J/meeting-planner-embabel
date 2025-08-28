package org.rag4j.meetingplanner.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.config.models.OpenAiModels;
import org.rag4j.meetingplanner.agent.model.Meeting;
import org.rag4j.meetingplanner.agent.model.MeetingRequest;
import org.rag4j.meetingplanner.agent.model.Person;
import org.rag4j.meetingplanner.agent.service.PersonFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Agent(
        name = "MeetingAgent",
        description = "An agent that helps to plan meetings by checking availability and scheduling them.",
        version = "1.0.0"
)
public record MeetingAgent(PersonFinder personFinder) {
    private static final Logger logger = LoggerFactory.getLogger(MeetingAgent.class);

    @AchievesGoal(description = "Book a meeting if all participants are available")
    @Action
    public Meeting bookMeeting(MeetingRequest request, OperationContext context) throws Exception {
        logger.info("Received meeting request: {}", request);

        Meeting response = context.ai().withLlm(OpenAiModels.GPT_41_MINI)
                .withToolObject(personFinder)
                .createObject(String.format("""
                                 You will be given a meeting request with participants emails.
                                 You can search for a participant by email using the 'findByEmail' tool.
                                 Each participant has an agenda with their availability.
                                 If not all participants are available, you can ask all participants when they are available.
                                 Choose a time when all participants are available and book the meeting.
                                 If you cannot find a time when all participants are available, respond with 'No common availability found'.
                                
                                 # Meeting request
                                 %s
                                
                                """,
                        request
                ).trim(), Meeting.class);
        logger.info("Response generated: {}", response);
        return response;

    }
}
