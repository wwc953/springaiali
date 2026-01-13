package org.example.agents.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import org.example.agents.service.SearchTool;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/hooks")
public class HooksController {

    String model= "deepseek-v3.2";

    @GetMapping("/chat")
    public void chat( ) throws GraphRunnerException {
        // 创建 DashScope API 实例
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(System.getenv("AI_DASHSCOPE_API_KEY"))
                .build();

        // 创建 ChatModel
        ChatModel chatModel = DashScopeChatModel.builder()
                .dashScopeApi(dashScopeApi)
                .defaultOptions(DashScopeChatOptions.builder()
                         .model(model)
                        .temperature(0.7)      // 控制随机性
                        .maxToken(2000)       // 最大输出长度
                        .topP(0.9)            // 核采样参数
                         .build())
                .build();

        // 创建工具（示例）
        ToolCallback sendEmailTool = createSendEmailTool();
        ToolCallback deleteDataTool = createDeleteDataTool();

        // 创建 Human-in-the-Loop Hook
        HumanInTheLoopHook humanReviewHook = HumanInTheLoopHook.builder()
                .approvalOn("sendEmailTool", ToolConfig.builder()
                        .description("Please confirm sending the email.")
                        .build())
                .approvalOn("deleteDataTool", ToolConfig.builder()
                        .description("Please confirm deleting the data.")
                        .build())
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("supervised_agent")
                .model(chatModel)
                .tools(sendEmailTool, deleteDataTool)
                .hooks(humanReviewHook)
                .saver(new MemorySaver())
                .build();

    }

    // ==================== 自定义 Hooks ====================
    private static ToolCallback createSendEmailTool() {
        return FunctionToolCallback.builder("sendEmailTool", (String input) -> "Email sent")
                .description("Send an email")
                .inputType(String.class)
                .build();
    }

    private static ToolCallback createDeleteDataTool() {
        return FunctionToolCallback.builder("deleteDataTool", (String input) -> "Data deleted")
                .description("Delete data")
                .inputType(String.class)
                .build();
    }

}
