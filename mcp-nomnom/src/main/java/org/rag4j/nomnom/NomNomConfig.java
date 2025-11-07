package org.rag4j.nomnom;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NomNomConfig {

    @Bean
    public VectorStore vectorStore(OpenAiApi openAiApi) {
        OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                .model(OpenAiApi.EmbeddingModel.TEXT_EMBEDDING_3_SMALL.getValue())
                .build();
        EmbeddingModel embeddingModel = new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);

        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public OpenAiApi openAiApi() {
        // read api keu y from environment variable OPENAI_API_KEY
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not set");
        }

        return OpenAiApi.builder()
                .apiKey(apiKey)
                .build();
    }
}
