package org.example.agents.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.fastjson.JSON;
import org.example.agents.service.SearchTool;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/agents")
public class AgentsController {

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
//                        .model(DashScopeModel.ChatModel.DEEPSEEK_V3.getValue())
                        .model(model)
                        .temperature(0.7)      // 控制随机性
                        .maxToken(2000)       // 最大输出长度
                        .topP(0.9)            // 核采样参数
//                        .enableThinking(true)
                        .build())
                .build();


        // 创建工具回调
        ToolCallback searchTool = FunctionToolCallback
                .builder("search", new SearchTool())
                .description("搜索信息的工具")
                .inputType(String.class)
                .build();


        // 使用 thread_id 维护对话上下文
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("user_123")
//                .addMetadata("key", "value")
                .build();


        String instruction = """
                你是一个经验丰富的软件架构师。
                                
                在回答问题时，请：
                1. 首先理解用户的核心需求
                2. 分析可能的技术方案
                3. 提供清晰的建议和理由
                4. 如果需要更多信息，主动询问
                                
                保持专业、友好的语气。
                """;

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
                .name("my_agent")
                .model(chatModel)
                .tools(searchTool)
                .systemPrompt("你是一个专业的技术助手。请准确、简洁地回答问题。")
                .instruction(instruction)// 更详细的指令
                .saver(new MemorySaver())// 配置记忆内存存储
                .build();

        AssistantMessage call = agent.call("我叫张三", runnableConfig);
        System.out.println(call);
        AssistantMessage text1 = agent.call("我叫什么名字？并请给出百度的网址", runnableConfig);// 输出: "你叫张三"
        System.out.println(text1);
    }

    @GetMapping("/chatTwo")
    public void chatTwo() throws GraphRunnerException {
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
                        .enableThinking(true)
                        .build())
                .build();

        ReactAgent agent = ReactAgent.builder()
                .name("my_agent")
                .model(chatModel)
                .build();

//          invoke 方法获取完整的执行状态：
        Optional<OverAllState> result = agent.invoke("帮我写一首诗");

        if (result.isPresent()) {
            OverAllState state = result.get();

            // 访问消息历史
            Optional<Object> messages = state.value("messages");
            List<Message> messageList = (List<Message>) messages.get();
//            System.out.println("消息历史：" + messageList);

            // 访问自定义状态
            Optional<Object> customData = state.value("custom_key");
//            System.out.println("自定义状态：" + customData.get());

            System.out.println("完整状态：" + state);
        }
    }


}
