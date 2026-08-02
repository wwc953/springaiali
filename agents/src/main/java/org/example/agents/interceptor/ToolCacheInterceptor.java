package org.example.agents.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具错误处理
 */
@Component
public class ToolCacheInterceptor extends ToolInterceptor {
    private Map<String, ToolCallResponse> cache = new ConcurrentHashMap<>();

    @Override
    public ToolCallResponse interceptToolCall(ToolCallRequest request, ToolCallHandler handler) {
        String cacheKey = generateCacheKey(request);

        // 检查缓存
        ToolCallResponse cached = cache.get(cacheKey);
        if (cached != null && !isExpired(cached)) {
            System.out.println("缓存命中: " + request.getToolName());
            return cached;
        }

        // 执行工具
        ToolCallResponse response = handler.call(request);

        // 缓存结果
        cache.put(cacheKey, response);

        return response;
    }

    @Override
    public String getName() {
        return "ToolCacheInterceptor";
    }

    private String generateCacheKey(ToolCallRequest request) {
        return request.getToolName() + ":" +
                request.getArguments();
    }

    private boolean isExpired(ToolCallResponse response) {
        // 实现 TTL 检查逻辑
        return false;
    }
}
