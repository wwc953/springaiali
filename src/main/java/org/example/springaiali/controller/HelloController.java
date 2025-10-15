package org.example.springaiali.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class HelloController {
    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";

    private final ChatClient chatClient;

    public HelloController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(MessageWindowChatMemory.builder().build()).build())//对话内容存在内存
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build()).build();
    }

    /**
     * ChatClient 简单调用
     */
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query) {
        return chatClient.prompt(query).call().content();
    }

    /**
     * ChatClient 流式调用
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(value = "query", defaultValue = "你好，很高兴认识你，能简单介绍一下自己吗？") String query, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return chatClient.prompt(query).stream().content();
    }

    /**
     * ChatClient 使用自定义的 Advisor 实现功能增强.
     * eg:
     * http://127.0.0.1:8080/advisor/chat/123?query=你好，我叫jack，之后的会话中都带上我的名字
     * 你好，jack！很高兴认识你。在接下来的对话中，我会记得带上你的名字。有什么想聊的吗？
     * http://127.0.0.1:8080/advisor/chat/123?query=我叫什么名字？
     * 你叫jack呀。有什么事情想要分享或者讨论吗，jack？
     * <p>
     * refer: https://docs.spring.io/spring-ai/reference/api/chat-memory.html#_memory_in_chat_client
     */
    @GetMapping("/advisor/chat/{conversationId}")
    public Flux<String> advisorChat(@PathVariable String conversationId,
                                    @RequestParam String query,
                                    HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return this.chatClient.prompt(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream().content();
    }


}
