package org.example.agents.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolCallResponse;
import com.alibaba.cloud.ai.graph.agent.interceptor.ToolInterceptor;
import org.springframework.stereotype.Component;

/**
 * ToolInterceptor - 监控和错误处理
 */
@Component
public class ToolMonitoringInterceptor extends ToolInterceptor {
    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        String toolName = request.getToolName();
        long startTime = System.currentTimeMillis();

        System.out.println("执行工具: " + toolName);

        try {
            ToolCallResponse response = handler.call(request);

            long duration = System.currentTimeMillis() - startTime;
            System.out.println("工具 " + toolName + " 执行成功 (耗时: " + duration + "ms)");

            return response;
        }
        catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("工具 " + toolName + " 执行失败 (耗时: " + duration + "ms): " + e.getMessage());

            return ToolCallResponse.of(
                    request.getToolCallId(),
                    request.getToolName(),
                    "工具执行失败: " + e.getMessage()
            );
        }
    }

    private void logSuccess(ToolCallRequest request, long duration) {
        System.out.println("Tool " + request.getToolName() + " succeeded in " + duration + "ms");
    }

    private void logError(ToolCallRequest request, Exception e, long duration) {
        System.err.println("Tool " + request.getToolName() + " failed in " + duration + "ms: " + e.getMessage());
    }

    @Override
    public String getName() {
        return "ToolMonitoringInterceptor";
    }
}
