package org.rag4j.meetingplanner.agent.meeting;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.common.StuckHandler;
import com.embabel.agent.api.common.StuckHandlerResult;
import com.embabel.agent.config.models.OpenAiModels;
import com.embabel.agent.core.AgentProcess;
import org.jetbrains.annotations.NotNull;
import org.rag4j.meetingplanner.agent.meeting.model.MeetingRequest;
import org.rag4j.meetingplanner.agent.meeting.model.MeetingResponse;
import org.rag4j.meetingplanner.agent.meeting.model.Participants;
import org.rag4j.meetingplanner.agent.meeting.model.Person;
import org.rag4j.meetingplanner.agent.meeting.service.PersonFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Agent(
        name = "MeetingAgent",
        description = "An agent that helps to plan meetings by checking availability and scheduling them.",
        version = "1.0.0"
)
public record MeetingAgent(PersonFinder personFinder) implements StuckHandler {
    private static final Logger logger = LoggerFactory.getLogger(MeetingAgent.class);


    @Action(description = "Find participants by their email addresses")
    public Participants findParticipants(MeetingRequest request) {
        logger.info("Find participants by email addresses");
        List<Person> participants = request.getParticipants().stream()
                .map(personFinder::findByEmail)
                .toList();

        return new Participants(participants);
    }

    @AchievesGoal(description = "Book a meeting if all participants are available")
    @Action
    public MeetingResponse bookMeeting(MeetingRequest request, Participants participants, OperationContext context) throws Exception {
        logger.info("Received meeting request: {}", request);

        MeetingResponse response = context.ai().withLlm(OpenAiModels.GPT_41_MINI)
                .withToolObject(participants)
                .createObject(String.format("""
                                 You will be given a meeting request with participants emails.
                                 You get availability information for each participant.
                                 Each participant has an agenda with their availability.
                                 If not all participants are available, you can ask all participants when they are available.
                                 Choose a time when all participants are available and book the meeting.
                                 If you cannot find a time when all participants are available, respond with 'No common availability found'.
                                
                                 # Meeting request
                                 %s

                                """,
                        request
                ).trim(), MeetingResponse.class);
        logger.info("Response generated: {}", response);
        return response;

    }

    @NotNull
    @Override
    public StuckHandlerResult handleStuck(@NotNull AgentProcess agentProcess) {
        logger.info("Received stuck request: {}", agentProcess);

        throw new RuntimeException("Oh my, I am stuck!");
    }
}
