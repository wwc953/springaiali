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

import java.util.List;

// ModelInterceptor - 内容安全检查
@Component
public class ContentModerationInterceptor extends ModelInterceptor {
    private static final Logger log = LoggerFactory.getLogger(ContentModerationInterceptor.class);
    private static final List<String> BLOCKED_WORDS =
            List.of("敏感词1", "敏感词2", "敏感词3");
    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        log.info("GuardrailInterceptor==");
        // 检查输入
        for (Message msg : request.getMessages()) {
            String content = msg.getText().toLowerCase();
            for (String blocked : BLOCKED_WORDS) {
                if (content.contains(blocked)) {
                    return ModelResponse.of(AssistantMessage.builder().content("检测到不适当的内容，请修改您的输入").build());
                }
            }
        }

        // 执行模型调用
        ModelResponse response = handler.call(request);

        // 检查输出
//        String output = response.getContent();
//        for (String blocked : BLOCKED_WORDS) {
//            if (output.contains(blocked)) {
//                // 清理输出
//                output = output.replaceAll(blocked, "[已过滤]");
//                return response.withContent(output);
//            }
//        }

        return response;
    }

    @Override
    public String getName() {
        return "ContentModerationInterceptor";
    }
}
