package org.example.springaiali.config;

import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import com.alibaba.cloud.ai.memory.jdbc.PostgresChatMemoryRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.PostgresChatMemoryRepositoryDialect;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ConditionalOnProperty(name = "vectorstore.type", havingValue = "pg")
public class PgConfig {

    @Value("${spring.ai.vectorstore.pgvector.initialize-schema}")
    private Boolean initializeSchema;

    @Bean
    public PgVectorStore pgVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(PgVectorStore.PgIndexType.HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(initializeSchema)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
    }


    /**
     * spring ai 方式
     * #spring.ai.chat.memory.repository.jdbc.initialize-schema=always
     * #spring.ai.chat.memory.repository.jdbc.schema=classpath:/static/pg.sql
     * @param jdbcTemplate
     * @return
     */
//    @Bean
//    public ChatMemoryRepository postgresChatMemoryRepository(JdbcTemplate jdbcTemplate) {
//        ChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder()
//                .jdbcTemplate(jdbcTemplate)
//                .dialect(new PostgresChatMemoryRepositoryDialect())
//                .build();
//        return chatMemoryRepository;
//    }
//

    /**
     * spring ai ali 方式
     * @param jdbcTemplate
     * @return
     */
    @Bean
    public PostgresChatMemoryRepository postgresChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        PostgresChatMemoryRepository postgresChatMemoryRepository = PostgresChatMemoryRepository.postgresBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
        return postgresChatMemoryRepository;
    }


    @Bean
    public ChatMemory postgresChatMemory(ChatMemoryRepository postgresChatMemoryRepository) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(postgresChatMemoryRepository)
                .maxMessages(15)
                .build();
        return chatMemory;

    }

}
