package org.rag4j.meetingplanner.webapp.chat;

import org.rag4j.meetingplanner.webapp.nomnom.NomNomAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

public class ChatAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(NomNomAgent.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public ChatAgent(ChatClient chatClient, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    public String sendMessage(String userId, String message) {
        LOGGER.info("Sending message to chat agent for userId = {}: {}", userId, message);
        String response = this.chatClient.prompt()
                .user(message)
                .advisors(MessageChatMemoryAdvisor.builder(this.chatMemory).conversationId(userId).build())
                .call()
                .content();
        LOGGER.info("Received response from chat agent for userId = {}: {}", userId, response);
        return response;
    }
}
