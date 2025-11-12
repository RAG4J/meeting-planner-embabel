package org.rag4j.meetingplanner.webapp.chat;

import com.embabel.agent.api.common.OperationContext;
import io.modelcontextprotocol.client.McpSyncClient;
import org.rag4j.meetingplanner.webapp.nomnom.NomNomAgent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChatConfig {
    @Bean("chatChatClient")
    public ChatClient chatClient(@Qualifier("chatChatModel") ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public ChatAgent chatAgent(@Qualifier("chatChatClient") ChatClient chatClient,
                               @Qualifier("chatChatMemory") ChatMemory chatMemory,
                               ChatTools tools) {
        return new ChatAgent(chatClient, chatMemory, tools);
    }

    @Bean("chatChatMemory")
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }


    @Bean("chatChatModel")
    public ChatModel chatModel(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(
                OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_5_MINI).build()
        ).build();
    }

}
