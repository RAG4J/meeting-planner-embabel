package org.rag4j.meetingplanner.webapp.controller;

import org.rag4j.meetingplanner.webapp.chat.ChatAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling chat interactions with Spring AI agent.
 * Maintains conversation memory per user session.
 */
@Controller
public class ChatController {
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private static final String CHAT_HISTORY_KEY = "chatHistory";

    private final ChatAgent chatAgent;
    private final ChatMemory chatMemory;

    public ChatController(ChatAgent chatAgent, @Qualifier("chatChatMemory") ChatMemory chatMemory) {
        this.chatAgent = chatAgent;
        this.chatMemory = chatMemory;
    }


    /**
     * Display the chat page.
     */
    @GetMapping("/chat")
    public String chat(Model model) {
        logger.info("Retrieving chat page");
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        model.addAttribute("title", "AI Chat");


        model.addAttribute("username", userId);
        model.addAttribute("email", "unknown");

        List<Message> messages = chatMemory.get(userId);
        List<ChatMessage> history = messages.stream().map(msg -> new ChatMessage(
                msg.getMessageType().getValue(),
                msg.getText(),
                msg.getMessageType().equals(MessageType.USER) ? userId : "Meeting Planner AI",
                LocalDateTime.now() // In a real app, you'd want to store and retrieve actual timestamps
        )).toList();

        // Load existing chat history
        model.addAttribute("chatHistory", history);

        return "chat";
    }

    /**
     * Handle chat message from user and return AI response.
     */
    @PostMapping("/chat/message")
    @ResponseBody
    public ChatResponse sendMessage(@RequestBody ChatRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            String userMessage = request.message();

            logger.info("Chat message from user {}: {}", userId, userMessage);

            var response = this.chatAgent.sendMessage(userId, userMessage);

            logger.info("Response message from assistant: {}", response);

            return new ChatResponse(response, true, null);

        } catch (Exception e) {
            logger.error("Error processing chat message", e);
            return new ChatResponse(null, false, "An error occurred: " + e.getMessage());
        }
    }

    /**
     * Clear chat history for the current session.
     */
    @PostMapping("/chat/clear")
    @ResponseBody
    public Map<String, Object> clearChat() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Clearing chat history for user {}", userId);
        chatMemory.clear(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    /**
     * Get chat history for the current session.
     */
    @GetMapping("/chat/history")
    @ResponseBody
    public List<ChatMessage> getChatHistory() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        logger.info("Retrieving chat history for user {}", userId);

        List<Message> messages = chatMemory.get(userId);
        return messages.stream().map(msg -> new ChatMessage(
                msg.getMessageType().getValue(),
                msg.getText(),
                msg.getMessageType().equals(MessageType.USER) ? userId : "Meeting Planner AI",
                LocalDateTime.now() // In a real app, you'd want to store and retrieve actual timestamps
        )).toList();
    }

    // DTOs

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String message, boolean success, String error) {
    }

    public record ChatMessage(
            String role,        // "user" or "assistant"
            String content,
            String sender,
            LocalDateTime timestamp
    ) {
    }
}
