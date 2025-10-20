package org.example.springaiali.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RagChatClientConfig {
    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

    @Autowired
    PgVectorStore pgVectorStore;

    @Bean
    public ChatClient ragDashScopeChatClient(DashScopeChatModel chatModel) {
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(pgVectorStore)
//                .searchRequest(SearchRequest.builder().similarityThreshold(0.8d).topK(6).build())
                .build();

        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);
        ChatClient chatClient = chatClientBuilder
                .defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .defaultAdvisors(questionAnswerAdvisor)
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build())
                .build();
        return chatClient;
    }
}
