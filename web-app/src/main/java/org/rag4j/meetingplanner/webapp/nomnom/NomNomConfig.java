package org.rag4j.meetingplanner.webapp.nomnom;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class NomNomConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public NomNomAgent nomnomAgent(ChatClient chatClient,
                                   ChatMemory chatMemory,
                                   List<McpSyncClient> mcpSyncClients) {
        return new NomNomAgent(chatClient, chatMemory,
                new SyncMcpToolCallbackProvider(mcpSyncClients).getToolCallbacks());
    }

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }


    @Bean
    ChatModel chatModel(OpenAiApi openAiApi) {
        return OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(
                OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_5_MINI).build()
        ).build();
    }

    @Bean
    public OpenAiApi openAIOkHttpClient() {
        var openAIApiKey = System.getenv("OPENAI_API_KEY");
        if (openAIApiKey == null || openAIApiKey.isEmpty()) {
            throw new IllegalArgumentException("No proxy is configured and no OPENAI_API_KEY environment variable has" +
                    " been set");
        }
        return OpenAiApi.builder()
                .apiKey(openAIApiKey)
                .build();
    }

}
