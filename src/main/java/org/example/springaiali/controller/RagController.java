package org.example.springaiali.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/rag")
public class RagController {

    @Autowired
    @Qualifier("ragDashScopeChatClient")
    ChatClient chatClient;

    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(value = "query", defaultValue = "讲个笑话") String msg) {
         ChatClient.CallResponseSpec call = chatClient.prompt()
                 .user(msg)
                 .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "type == 'Spring'"))
                 .call();
        ChatResponse chatResponse = call.chatResponse();
        System.out.println("chatResponse《======" + chatResponse);
        return chatResponse.getResult().getOutput().getText();
    }


}
