package org.example.agents.interceptor;

import com.alibaba.cloud.ai.graph.agent.interceptor.ModelCallHandler;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelRequest;
import com.alibaba.cloud.ai.graph.agent.interceptor.ModelResponse;
import org.example.agents.model.UserPreferenceStore;
import org.example.agents.model.UserPreferences;
import org.springframework.ai.chat.messages.SystemMessage;

/**
 *  基于存储的个性化提示
 *
 * 从长期记忆加载用户偏好并生成个性化提示
 */
public class PersonalizedPromptInterceptor extends ModelInterceptor {
    private final UserPreferenceStore store;

    public PersonalizedPromptInterceptor(UserPreferenceStore store) {
        this.store = store;
    }

    @Override
    public ModelResponse interceptModel(ModelRequest request, ModelCallHandler handler) {
        // 从运行时上下文获取用户ID
        String userId = getUserIdFromContext(request);

        // 从存储加载用户偏好
        UserPreferences prefs = store.getPreferences(userId);

        // 构建个性化提示
        String personalizedPrompt = buildPersonalizedPrompt(prefs);

        // 更新系统消息（参考 TodoListInterceptor 的实现方式）
        SystemMessage enhancedSystemMessage;
        if (request.getSystemMessage() == null) {
            enhancedSystemMessage = new SystemMessage(personalizedPrompt);
        } else {
            enhancedSystemMessage = new SystemMessage(
                    request.getSystemMessage().getText() + "\n\n" + personalizedPrompt
            );
        }

        // 创建增强的请求
        ModelRequest enhancedRequest = ModelRequest.builder(request)
                .systemMessage(enhancedSystemMessage)
                .build();

        // 调用处理器
        return handler.call(enhancedRequest);
    }

    private String getUserIdFromContext(ModelRequest request) {
        // 相当于是从 RunnableConfig 中读取提取用户ID，所以agent调用时要设置 user-id
        return (String) request.getContext().get("user-id"); // 简化示例
    }

    private String buildPersonalizedPrompt(UserPreferences prefs) {
        StringBuilder prompt = new StringBuilder("你是一个有用的助手。");

        if (prefs.getCommunicationStyle() != null) {
            prompt.append("\n沟通风格：").append(prefs.getCommunicationStyle());
        }

        if (prefs.getLanguage() != null) {
            prompt.append("\n使用语言：").append(prefs.getLanguage());
        }

        if (!prefs.getInterests().isEmpty()) {
            prompt.append("\n用户兴趣：").append(String.join(", ", prefs.getInterests()));
        }

        return prompt.toString();
    }

    @Override
    public String getName() {
        return "PersonalizedPromptInterceptor";
    }
}
