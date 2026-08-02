package org.example.agents.service;

import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.hook.Hook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.alibaba.cloud.ai.graph.agent.hook.hip.ToolConfig;
import com.alibaba.cloud.ai.graph.agent.hook.modelcalllimit.ModelCallLimitHook;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIDetectionHook;
import com.alibaba.cloud.ai.graph.agent.hook.pii.PIIType;
import com.alibaba.cloud.ai.graph.agent.hook.pii.RedactionStrategy;
import com.alibaba.cloud.ai.graph.agent.hook.summarization.SummarizationHook;
import com.alibaba.cloud.ai.graph.agent.interceptor.Interceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.todolist.TodoListInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolretry.ToolRetryInterceptor;
import com.alibaba.cloud.ai.graph.agent.interceptor.toolselection.ToolSelectionInterceptor;
import com.alibaba.cloud.ai.graph.checkpoint.savers.redis.RedisSaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.streaming.OutputType;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.agents.hook.AdvancedMessageTrimmingHook;
import org.example.agents.hook.AgentLoggingHook;
import org.example.agents.hook.CustomModelHook;
import org.example.agents.hook.SimpleMessageTrimmingHook;
import org.example.agents.interceptor.*;
import org.example.agents.model.AgentRunResponse;
import org.example.agents.tool.SearchTool;
import org.example.agents.tool.SendEmailTool;
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

import java.util.List;

@Slf4j
@Component
public class AgentsService {
    @Resource
    DynamicPromptInterceptor dynamicPromptInterceptor;
    @Resource
    ContentModerationInterceptor guardrailInterceptor;
    @Resource
    ToolCacheInterceptor myToolErrorInterceptor;
    @Resource
    ToolMonitoringInterceptor toolMonitoringInterceptor;
    @Resource
    Redisson redisson;
    @Resource
    ObjectMapper mapper;

    @Resource
    ChatModel qwenChatModel;

    @Resource
    AgentLoggingHook loggingHook;

    @Resource
    SimpleMessageTrimmingHook messageTrimmingHook;

    @Resource
    CustomModelHook customModelHook;

    public Flux<ServerSentEvent<String>> chatAgent(String umsg, HttpServletResponse httpServletResponse) throws GraphRunnerException {
        log.info("umsg==>{}", umsg);
        httpServletResponse.setCharacterEncoding("UTF-8");

        // 创建工具回调
        ToolCallback searchTool = FunctionToolCallback
                .builder("search", new SearchTool())
                .description("搜索信息的工具")
                .inputType(String.class)
                .build();

        // 创建工具回调
        ToolCallback sendEmailTool = FunctionToolCallback
                .builder("sendEmailTool", new SendEmailTool())
                .description("发送电子邮件功能")
                .inputType(String.class)
                .build();

        List<ToolCallback> toolList = List.of(searchTool, sendEmailTool);

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

        // 内置的消息压缩 Hook
        SummarizationHook summarizationHook = SummarizationHook.builder()
                .model(qwenChatModel)
                .maxTokensBeforeSummary(4000)//触发摘要之前的最大 token 数
                .messagesToKeep(20)//摘要后保留的最新消息数
                .build();

        // 创建 Human-in-the-Loop Hook 暂停 Agent 执行以获得人工批准、编辑或拒绝工具调用
        /**
         * 适用场景：
         * 需要人工批准的高风险操作（数据库写入、金融交易）；
         * 人工监督是强制性的合规工作流程；
         * 长期对话，使用人工反馈引导 Agent。
         */
        HumanInTheLoopHook humanReviewHook = HumanInTheLoopHook.builder()
                .approvalOn("sendEmailTool", ToolConfig.builder()
                        .description("Please confirm sending the email.")
                        .build())
                .approvalOn("deleteDataTool", ToolConfig.builder()
                        .description("Please confirm deleting the data.")
                        .build())
                .build();

        //限制模型调用次数hook
        ModelCallLimitHook modelCallLimitHook = ModelCallLimitHook.builder().runLimit(5).build();

        /**
         * 检测和处理对话中的个人身份信息。
         *
         * 适用场景：
         *
         * 具有合规要求的医疗保健和金融应用；
         * 需要清理日志的客户服务 Agent；
         * 任何处理敏感用户数据的应用程序。
         */
        PIIDetectionHook pii = PIIDetectionHook.builder()
                .piiType(PIIType.EMAIL)
                .strategy(RedactionStrategy.REDACT)
                .applyToInput(true)
                .build();

        List<Hook> hookList = List.of(loggingHook, messageTrimmingHook, summarizationHook, humanReviewHook, modelCallLimitHook, pii, customModelHook,
                new AdvancedMessageTrimmingHook());


        //工具调用重试
        /**
         * 自动重试失败的工具调用，具有可配置的指数退避。
         *
         * 适用场景：
         *
         * 处理外部 API 调用中的瞬态故障；
         * 提高依赖网络的工具的可靠性；
         * 构建优雅处理临时错误的弹性 Agent。
         */
        ToolRetryInterceptor toolRetryInterceptor = ToolRetryInterceptor.builder()
                .maxRetries(2)
                .onFailure(ToolRetryInterceptor.OnFailureBehavior.RETURN_MESSAGE)
                .build();

        /**
         * 在执行工具之前强制执行一个规划步骤，以概述 Agent 将要采取的步骤。
         *
         * 适用场景：
         *
         * 需要执行复杂、多步骤任务的 Agent；
         * 通过在执行前显示 Agent 的计划来提高透明度；
         * 通过检查建议的计划来调试错误。
         */
        TodoListInterceptor todoListInterceptor = TodoListInterceptor.builder().build();

        /**
         * 使用一个 LLM 来决定在多个可用工具之间选择哪个工具。
         *
         * 适用场景：
         *
         * 当多个工具可以实现相似目标时；
         * 需要根据细微的上下文差异进行工具选择；
         * 动态选择最适合特定输入的工具。
         */
        ToolSelectionInterceptor toolSelectionInterceptor = ToolSelectionInterceptor.builder().selectionModel(qwenChatModel).build();

        List<Interceptor> interceptorList = List.of(dynamicPromptInterceptor, guardrailInterceptor, myToolErrorInterceptor, toolMonitoringInterceptor,
                toolRetryInterceptor, todoListInterceptor, toolSelectionInterceptor,
                new ModelMonitoringInterceptor());

        // 创建 Agent
        ReactAgent agent = ReactAgent.builder()
                .name("my_agent")
                .model(qwenChatModel)
                .tools(toolList)
                .hooks(hookList)
                .systemPrompt("你是一个专业的技术助手。请准确、简洁地回答问题。")
//                .instruction(instruction)// 更详细的指令
                .interceptors(interceptorList)
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
