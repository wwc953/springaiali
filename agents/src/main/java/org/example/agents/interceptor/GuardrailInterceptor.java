package org.example.agents.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

// ModelInterceptor - 内容安全检查
@Component
public class GuardrailInterceptor extends ModelInterceptor {
    private static final Logger log = LoggerFactory.getLogger(GuardrailInterceptor.class);

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        log.info("GuardrailInterceptor==");
        // 前置：检查输入
        if (containsSensitiveContent(request.getMessages())) {
            log.info("检测到不适当的内容***");
            return ModelResponse.of(new AssistantMessage("检测到不适当的内容"));
        }

        // 执行调用
        ModelResponse response = handler.call(request);

        // 后置：检查输出
        return sanitizeIfNeeded(response);
    }

    private boolean containsSensitiveContent(List<Message> messages) {
        // 实现敏感内容检测逻辑
        for (Message message : messages) {
            if (message instanceof UserMessage) {
                log.info("用户输入敏感词检查：{}", message.getText());
                if (message.getText().contains("123"))
                    return true;
            }
        }
        return false;
    }

    private ModelResponse sanitizeIfNeeded(ModelResponse response) {
        // 实现响应清理逻辑
        return response;
    }

    @Override
    public String getName() {
        return "GuardrailInterceptor";
    }
}
