package org.example.agents.service;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.agents.interceptor.DynamicPromptInterceptor;
import org.example.agents.interceptor.GuardrailInterceptor;
import org.example.agents.interceptor.MyToolErrorInterceptor;
import org.example.agents.interceptor.ToolMonitoringInterceptor;
import org.example.agents.model.AgentRunResponse;
import org.redisson.Redisson;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class AgentsService {
    @Resource
    DynamicPromptInterceptor dynamicPromptInterceptor;
    @Resource
    GuardrailInterceptor guardrailInterceptor;
    @Resource
    MyToolErrorInterceptor myToolErrorInterceptor;
    @Resource
    ToolMonitoringInterceptor toolMonitoringInterceptor;
    @Resource
    Redisson redisson;
    @Resource
    ObjectMapper mapper;

    @Resource
    ChatModel qwenChatModel;

    public Flux<ServerSentEvent<String>> chatAgent(String umsg, HttpServletResponse httpServletResponse) throws GraphRunnerException {
        log.info("umsg==>{}", umsg);
        httpServletResponse.setCharacterEncoding("UTF-8");

        // 创建工具回调
        ToolCallback searchTool = FunctionToolCallback
                .builder("search", new SearchTool())
                .description("搜索信息的工具")
                .inputType(String.class)
                .build();


        // 使用 thread_id 维护对话上下文
        // 1. 构建配置：标记用户和请求来源
        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("user_123")
                .addMetadata("user_id", "10086")
                .addMetadata("request_source", "mobile_app")
                .addMetadata("max_retries", 3)    // 自定义重试次数
                .addMetadata("timeout_seconds", 30)
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
                .model(qwenChatModel)
                .tools(searchTool)
                .systemPrompt("你是一个专业的技术助手。请准确、简洁地回答问题。")
//                .instruction(instruction)// 更详细的指令
                .interceptors(dynamicPromptInterceptor, guardrailInterceptor, myToolErrorInterceptor, toolMonitoringInterceptor)
                .saver(RedisSaver.builder().redisson(redisson).build())// 配置记忆内存存储
//                .saver(new MemorySaver())
                .build();

        // 多个消息
//        List<Message> messages = List.of(
//                new UserMessage("我想了解 Java 多线程"),
//                new UserMessage("特别是线程池的使用")
//        );

        // 流式输出
        return agent.stream(umsg, runnableConfig)
                .map(nodeOutput -> {
                            // 当前执行的节点名称
                            String node = nodeOutput.node();
                            // Agent 名称
                            String agentName = nodeOutput.agent();
                            // Token 消耗统计
                            Usage tokenUsage = nodeOutput.tokenUsage();
                            // 最终返回给前端的响应对象
                            AgentRunResponse agentResponse = null;

                            // ====================== 处理流式输出 ======================
                            if (nodeOutput instanceof StreamingOutput<?> streamingOutput) {
                                OutputType type = streamingOutput.getOutputType();
                                // 获取 AI 输出的消息对象
                                Message message = streamingOutput.message();

                                // 无消息内容时返回空 JSON
                                if (message == null) {
                                    return ServerSentEvent.<String>builder().data("{}").build();
                                }

                                // 处理模型流式输出
                                if (type == OutputType.AGENT_MODEL_STREAMING) {
                                    if (message instanceof AssistantMessage assistantMessage) {
                                        String text = null;
                                        // 检查是否为 Thinking 消息
                                        Object reasoningContent = assistantMessage.getMetadata().get("reasoningContent");
                                        if (reasoningContent != null && !reasoningContent.toString().isEmpty()) {
//                                            System.out.print("[Thinking] " + reasoningContent);
                                            text = "[Thinking] " + reasoningContent;
                                        } else {
                                            // 普通模型响应（增量内容）
//                                            System.out.print(assistantMessage.getText());
                                            text = assistantMessage.getText();
                                        }
                                        System.out.print(text);
//                                        return ServerSentEvent.<String>builder().data(text).build();

                                        // 普通文本消息（流式打字输出）
                                        agentResponse = new AgentRunResponse(
                                                node, agentName, assistantMessage, tokenUsage, text);
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
//                                            System.out.println("\n[Model Finished]");
                                            agentResponse = new AgentRunResponse(
                                                    node, agentName, assistantMessage, tokenUsage, "\n[Model Finished]");
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
//                            return ServerSentEvent.<String>builder().data("").build();
                            try {
                                if (agentResponse != null) {
                                    // 对象转 JSON 字符串
                                    String jsonData = mapper.writeValueAsString(agentResponse);
                                    // 封装成标准 SSE 事件返回
                                    return ServerSentEvent.<String>builder().data(jsonData).build();

                                }
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e);
                            }
                            // 默认空消息
                            return ServerSentEvent.<String>builder().data("{}").build();
                        }
                );
    }
}
