package org.example.agents.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public DashScopeApi dashScopeApi() {
        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();
        return dashScopeApi;
    }

    @Bean
    public DashScopeChatModel qwenChatModel(DashScopeApi dashScopeApi) {
        // 创建 ChatModel
        DashScopeChatModel dashScopeChatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                        .model("qwen3.7-max")
                        .temperature(0.7)      // 控制随机性
                        .maxToken(2000)       // 最大输出长度
                        .topP(0.9)            // 核采样参数
                        .enableThinking(false)//是否开启思考模式，默认开启
                        .build())
                .build();
        return dashScopeChatModel;
    }

}
