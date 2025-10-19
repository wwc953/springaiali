package org.example.springaiali.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

    @Autowired
    ChatMemory postgresChatMemory;

    @Bean
    public ChatClient dashScopeChatClient(DashScopeChatModel chatModel) {
        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel);
        ChatClient chatClient = chatClientBuilder
                .defaultSystem(DEFAULT_PROMPT)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())//对话内容存在内存MessageWindowChatMemory.builder().build()
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(postgresChatMemory).build())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
//        chatModel.getDashScopeChatOptions()
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build())
                .build();
        return chatClient;
//        return ChatClient.create(chatModel);
    }
}
