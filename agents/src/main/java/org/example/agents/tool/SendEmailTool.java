package org.example.agents.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

 public class SendEmailTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(
            @ToolParam(description = "发送电子邮件") String email,
            ToolContext toolContext) {
        return "发送成功：" + email;
    }
}
