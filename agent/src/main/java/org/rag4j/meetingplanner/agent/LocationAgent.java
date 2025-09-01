package org.rag4j.meetingplanner.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.config.models.OpenAiModels;
import org.rag4j.meetingplanner.agent.model.location.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Agent(
        name = "LocationAgent",
        description = "An agent that helps to find appropriate meeting locations.",
        version = "1.0.0"
)
public class LocationAgent {
    private static final Logger logger = LoggerFactory.getLogger(LocationAgent.class);

    @AchievesGoal(description = "Book a meeting room at a location for the specified number of people.")
    @Action(toolGroups = {"location"}, description = "Book the available room")
    public BookingResult bookRoom(SuggestedRoom room, OperationContext context) throws Exception {
        logger.info("Received book room request: {}", room);

        BookingResult response = context.ai().withLlm(OpenAiModels.GPT_41_MINI)
                .createObject(String.format("""
                                 You will be given an suggested room.
                                 If the id of the room is 'non-available', write the response message that it did not work and stop processing.
                                 You have access to all rooms for that location through tools.
                                 Assumed the room is available, book the room using the provided tools.
                                 Return a description of the booking that was created or a message if it failed.
                                
                                 # room to book
                                 %s

                                """,
                        room
                ).trim(), BookingResult.class);
        logger.info("Response generated: {}", response);
        return response;
    }

    @Action(toolGroups = {"location"}, description = "Check availability of a room at the preferred location")
    public SuggestedRoom findRoomAtLocation(Location location, RoomRequest roomRequest, OperationContext context) {
        logger.info("Received find room request: {}", roomRequest);

        SuggestedRoom response = context.ai().withLlm(OpenAiModels.GPT_41_MINI)
                .createObject(String.format("""
                                 You will be given an Id for a location.
                                 You have access to all rooms for that location through tools.
                                 Check availability of a room at the preferred location.
                                 Return the best matching room, or roomId "not-available".
                                
                                 # LocationId
                                 %s
                                 # Number of participants
                                 %d
                                 # Date, start time, number of minutes
                                 %s, %s, %d

                                """,
                        location.id(), roomRequest.numberOfParticipants(), roomRequest.date(), roomRequest.startTime(), roomRequest.numberOfMinutes()
                ).trim(), SuggestedRoom.class);
        logger.info("Response generated: {}", response);
        return response;
    }

    @Action(toolGroups = {"location"}, description = "Find the best matching location for a meeting based on the provided description.")
    public Location findLocation(RoomRequest request, OperationContext context) {
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
                        request.locationDescription()
                ).trim(), Location.class);
        logger.info("Response generated: {}", response);
        return response;
    }

}
