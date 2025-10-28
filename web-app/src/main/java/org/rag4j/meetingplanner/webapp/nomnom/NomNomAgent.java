package org.rag4j.meetingplanner.webapp.nomnom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;


public class NomNomAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(NomNomAgent.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ToolCallback[] toolCallbacks;


    public NomNomAgent(ChatClient chatClient, ChatMemory chatMemory, ToolCallback[] toolCallbacks) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.toolCallbacks = toolCallbacks;
    }

    public OrderConfirmation placeOrder(String location, LocalDate deliveryDate, String orderDetails) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        LOGGER.info("Placing NomNom order for userId = {}, orderDetails = {}", userId, orderDetails);

        String prompt = """
                You are an AI agent that helps users place food orders through the NomNom service.
                You will be given order details including location, date, and specific food and beverage requests.
                Use the tools to place an order. If you are requested to confirm the order, ask for the requested
                information if it is okay to proceed.
                If the user says yes, confirm the order a requested by the tool.
                If the user says no, cancel the order as requested by the tool.
                """;

        String userMessage = String.format("""
                Place an order with the following details:
                Location: %s
                Delivery Date: %s
                Order Details: %s
                """, location, deliveryDate, orderDetails);

        OrderConfirmation orderConfirmation = this.chatClient.prompt()
                .system(prompt)
                .user(userMessage)
                .toolCallbacks(this.toolCallbacks)
                .advisors(MessageChatMemoryAdvisor.builder(this.chatMemory).conversationId(userId).build())
                .call()
                .entity(OrderConfirmation.class);

        assert orderConfirmation != null;
        LOGGER.info("The response is a confirmation message: {}, the message is '{}'",
                orderConfirmation.askForConfirmation(), orderConfirmation.responseMessage());
        return orderConfirmation;
    }

    public OrderConfirmation confirmOrder(String confirmationMessage) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        LOGGER.info("Confirming NomNom order for userId = {}, confirmation = {}", userId, confirmationMessage);

        String prompt = """
                You are an AI agent that helps users place food orders through the NomNom service.
                You will be given order details including location, date, and specific food and beverage requests.
                Use the tools to place an order. If you are requested to confirm the order, ask for the requested
                information if it is okay to proceed.
                If the user says yes, confirm the order a requested by the tool.
                If the user says no, cancel the order as requested by the tool.
                """;

        OrderConfirmation orderConfirmation = this.chatClient.prompt()
                .system(prompt)
                .user(confirmationMessage)
                .toolCallbacks(this.toolCallbacks)
                .advisors(MessageChatMemoryAdvisor.builder(this.chatMemory).conversationId(userId).build())
                .call()
                .entity(OrderConfirmation.class);

        assert orderConfirmation != null;
        LOGGER.info("The response of the confirmation is '{}'", orderConfirmation.responseMessage());
        return orderConfirmation;
    }


    public record OrderConfirmation(boolean askForConfirmation, String responseMessage) {
    }

}
