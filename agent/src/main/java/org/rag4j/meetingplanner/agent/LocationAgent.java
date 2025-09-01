package org.rag4j.meetingplanner.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.config.models.OpenAiModels;
import org.rag4j.meetingplanner.agent.model.location.Location;
import org.rag4j.meetingplanner.agent.model.location.LocationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Agent(
        name = "LocationAgent",
        description = "An agent that helps to find appropriate meeting locations.",
        version = "1.0.0"
)
public class LocationAgent {
    private static final Logger logger = LoggerFactory.getLogger(LocationAgent.class);

    @AchievesGoal(description = "Book a meeting if all participants are available")
    @Action(toolGroups = {"location"}, description = "Find the best matching location for a meeting based on the provided description.")
    public Location bookMeeting(LocationRequest request, OperationContext context) throws Exception {
        logger.info("Received meeting request: {}", request);

        Location response = context.ai().withLlm(OpenAiModels.GPT_41_MINI)
                .createObject(String.format("""
                                 You will be given a description for a location.
                                 You have access to all available locations through tools.
                                 Match the provided description to the available locations to find the best match.
                                 Return the best matching location, always suggest one of the locations.
                                 Stick to found locations, do not make up new ones.
                                
                                 # Location description
                                 %s

                                """,
                        request.description()
                ).trim(), Location.class);
        logger.info("Response generated: {}", response);
        return response;

    }

}
