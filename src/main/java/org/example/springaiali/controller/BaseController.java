package org.example.springaiali.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/base")
public class BaseController {

    @Autowired
    @Qualifier("dashScopeChatClient")
    ChatClient chatClient;

    /**
     * ChatClient 简单调用
     */
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "讲个笑话") String msg) {
//        return chatClient.prompt(msg).call().content();
        ChatClient.CallResponseSpec call = chatClient.prompt().user(msg).call();
        ChatResponse chatResponse = call.chatResponse();
        System.out.println("chatResponse《======" + chatResponse);
        return chatResponse.getResult().getOutput().getText();
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
