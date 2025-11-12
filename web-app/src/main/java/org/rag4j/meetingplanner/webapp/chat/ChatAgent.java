package org.rag4j.meetingplanner.webapp.chat;

import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.core.Agent;
import org.rag4j.meetingplanner.webapp.nomnom.NomNomAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.List;

public class ChatAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(NomNomAgent.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ChatTools chatTools;

    public ChatAgent(ChatClient chatClient, ChatMemory chatMemory, ChatTools chatTools) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.chatTools = chatTools;
    }

    public String sendMessage(String userId, String message) {
        LOGGER.info("Sending message to chat agent for userId = {}: {}", userId, message);

        String systemPrompt = """
                You are an AI chat agent that assists users with planning meetings and answering questions.
                You have access to various tools and agents to help you provide accurate and helpful responses.
                You can book a meeting between persons, find a location for a meeting, and order food for the meeting.
                You have access to the input objects to kickstart the agents.
                Ask questions if it is not clear what to do. If information is incomplete, ask for more information.
                Use the available tool to call the right agent.
                Always try to book the meeting, a location and order food for the meeting.
                
                Below are the agents, what they can do, and how to use them:
                LocationAgent: Helps to find and book meeting locations.
                Input class: LocationInput {String locationName, int capacity, String dateTime}
                
                FoodAndDrinksAgent: Helps to order food for meetings.
                Input class: NomNomOrderRequest {String location, LocalDate deliveryDate, String message}
                
                MeetingAgent: Helps to schedule meetings between participants.
                Input class public MeetingRequest {String title, String description, LocalDate date, LocalTime startTime}
                
                When responding to user messages, consider the context of the conversation and utilize the available agents as needed.
                Stick to the data provided by the request or responses from the agents. So do not change product names, locations, dates, times, etc.
                Keep the conversation relevant to meeting planning and related topics. Keep the number of words low.
                """;

        String response = this.chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .tools(this.chatTools)
                .advisors(MessageChatMemoryAdvisor.builder(this.chatMemory).conversationId(userId).build())
                .call()
                .content();
        LOGGER.info("Received response from chat agent for userId = {}: {}", userId, response);

        return response;
    }

}
