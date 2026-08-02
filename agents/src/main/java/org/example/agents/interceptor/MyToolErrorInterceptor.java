package org.example.agents.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.*;
import org.springframework.stereotype.Component;

/**
 * 工具错误处理
 */
@Component
public class MyToolErrorInterceptor extends ToolInterceptor {

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        try {
            return handler.call(request);
        } catch (Exception e) {
            return ToolCallResponse.of(request.getToolCallId(), request.getToolName(),
                    "Tool failed: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "ToolErrorInterceptor";
    }
}
