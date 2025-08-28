package org.rag4j.meetingplanner.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.common.StuckHandler;
import com.embabel.agent.api.common.StuckHandlerResult;
import com.embabel.agent.api.common.StuckHandlingResultCode;
import com.embabel.agent.config.models.OpenAiModels;
import com.embabel.agent.core.AgentProcess;
import org.jetbrains.annotations.NotNull;
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
public record MeetingAgent(PersonFinder personFinder) implements StuckHandler {
    private static final Logger logger = LoggerFactory.getLogger(MeetingAgent.class);


//    @Action(description = "Find participants by their email addresses")
//    public List<Person> findParticipants(MeetingRequest request) {
//        logger.info("Find participants by email addresses");
//        return request.getParticipants().stream()
//                .map(personFinder::findByEmail)
//                .toList();
//    }

//    @Action(description = "Check availability of all participants for a given day and time range")
//    public String checkAllAvailable(MeetingRequest request, List<Person> participants) {
//        logger.info("Check availability of all participants for the meeting request: {}", request);
//        boolean allAvailable = participants.stream()
//                .allMatch(person -> person.checkAvailability(
//                        request.getDate(),
//                        request.getStartTime(),
//                        request.getStartTime().plusMinutes(request.getDurationMinutes())
//                ));
//
//        if (allAvailable) {
//            return "All participants are available.";
//        } else {
//            return "Not all participants are available.";
//        }
//    }


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

    @NotNull
    @Override
    public StuckHandlerResult handleStuck(@NotNull AgentProcess agentProcess) {
        logger.info("Received stuck request: {}", agentProcess);

        throw new RuntimeException("Oh my, I am stuck!");
//        return new StuckHandlerResult("Sorry, I am unable to process your request at the moment.", this, StuckHandlingResultCode.REPLAN, agentProcess);
    }
}
