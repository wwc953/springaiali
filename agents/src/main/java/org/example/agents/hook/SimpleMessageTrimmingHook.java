package org.example.agents.hook;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import com.alibaba.cloud.ai.graph.agent.hook.messages.AgentCommand;
import com.alibaba.cloud.ai.graph.agent.hook.messages.MessagesModelHook;
import com.alibaba.cloud.ai.graph.agent.hook.messages.UpdatePolicy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MessagesModelHook - 在模型调用前修剪消息
 * 使用 MessagesModelHook 实现，在模型调用前修剪消息列表，只保留最后 MAX_MESSAGES 条消息
 */
@Component
@Slf4j
@HookPositions({HookPosition.BEFORE_MODEL})
public class SimpleMessageTrimmingHook extends MessagesModelHook {
    private static final int MAX_MESSAGES = 3;

    @Override
    public String getName() {
        return "simple_message_trimming_MessagesModelHook";
    }

    @Override
    public AgentCommand beforeModel(List<Message> previousMessages, RunnableConfig config) {
        log.info("previousMessages长度：{}", previousMessages.size());
        // 如果消息数量超过限制，只保留最后 MAX_MESSAGES 条消息
        if (previousMessages.size() > MAX_MESSAGES) {
            List<Message> trimmedMessages = previousMessages.subList(
                    previousMessages.size() - MAX_MESSAGES,
                    previousMessages.size()
            );
            // 使用 REPLACE 策略替换所有消息
            return new AgentCommand(trimmedMessages, UpdatePolicy.REPLACE);
        }
        // 如果消息数量未超过限制，返回原始消息（不进行修改）
        return new AgentCommand(previousMessages);
    }
}
