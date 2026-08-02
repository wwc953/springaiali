package org.example.agents.controller;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolerror.ToolErrorInterceptor;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.example.agents.interceptor.DynamicPromptInterceptor;
import org.example.agents.model.AgentRunResponse;
import org.example.agents.service.SearchTool;
import org.redisson.Redisson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/agentsBase")
public class AgentsBaseController {

    private static final Logger log = LoggerFactory.getLogger(AgentsBaseController.class);


    String model = "qwen3.7-max";

    @GetMapping("/chatex")
    public void chatex(@RequestParam("umsg") String umsg) throws GraphRunnerException {
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
                        .enableThinking(false)//是否开启思考模式，默认开启
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
                .interceptors(new ToolErrorInterceptor())//ToolErrorInterceptor 工具错误处理 可继承ToolInterceptor自定义
                .interceptors(new DynamicPromptInterceptor())//动态提示词
                .saver(new MemorySaver())// 配置记忆内存存储
                .build();

        // 多个消息
        List<Message> messages = List.of(
                new UserMessage("我想了解 Java 多线程"),
                new UserMessage("特别是线程池的使用")
        );

        // 流式输出
        Flux<NodeOutput> stream = agent.stream(messages, runnableConfig);

        stream.subscribe(
                output -> {
                    // 结合 OutputType 和消息类型进行处理
                    if (output instanceof StreamingOutput streamingOutput) {
                        OutputType type = streamingOutput.getOutputType();
                        Message message = streamingOutput.message();

                        // 处理模型流式输出
                        if (type == OutputType.AGENT_MODEL_STREAMING) {
                            if (message instanceof AssistantMessage assistantMessage) {
                                // 检查是否为 Thinking 消息
                                Object reasoningContent = assistantMessage.getMetadata().get("reasoningContent");
                                if (reasoningContent != null && !reasoningContent.toString().isEmpty()) {
                                    System.out.print("[Thinking] " + reasoningContent);
                                } else {
                                    // 普通模型响应（增量内容）
                                    System.out.print(assistantMessage.getText());
                                }
                            }
                        }
                        // 处理模型输出完成
                        else if (type == OutputType.AGENT_MODEL_FINISHED) {
                            if (message instanceof AssistantMessage assistantMessage) {
                                if (assistantMessage.hasToolCalls()) {
                                    // 工具调用请求
                                    assistantMessage.getToolCalls().forEach(toolCall -> {
                                        System.out.println("[Tool Call] " + toolCall.name() + ": " + toolCall.arguments());
                                    });
                                } else {
                                    // 模型完整响应
                                    System.out.println("\n[Model Finished]");
                                }
                            }
                        }
                        // 处理工具执行结果
                        else if (type == OutputType.AGENT_TOOL_FINISHED) {
                            if (message instanceof ToolResponseMessage toolResponse) {
                                toolResponse.getResponses().forEach(response -> {
                                    System.out.println("[Tool Result] " + response.name() + ": " + response.responseData());
                                });
                            }
                        }
                    }
                },
                error -> System.err.println("错误: " + error.getMessage()),
                () -> System.out.println("流式输出完成end")
        );

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
