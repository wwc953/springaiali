package org.example.agents.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.ai.chat.messages.AbstractMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.Usage;

public class AgentRunResponse {

    /**
     * 当前执行的节点名称
     */
    @JsonProperty("node")
    protected String node;

    /**
     * 执行的智能体名称
     */
    @JsonProperty("agent")
    protected String agent;

    /**
     * Token 消耗统计信息
     */
    @JsonProperty("tokenUsage")
    protected Usage tokenUsage;

    /**
     * 对话消息 DTO 对象
     * 用于序列化传输，避免原生 Message 序列化异常
     */
    @JsonProperty("message")
    protected AbstractMessage message;

    /**
     * 流式输出文本片段
     * SSE 逐字推送的核心字段
     */
    @JsonProperty("chunk")
    private String chunk;

    /**
     * 无参构造函数
     * 供 Jackson 反序列化使用
     */
    public AgentRunResponse() {
    }

    /**
     * 构造函数：通过 Spring AI 原生 Message 创建响应对象
     * @param node 执行节点
     * @param agent 智能体名称
     * @param message Spring AI 原生消息
     * @param tokenUsage Token 消耗
     * @param chunk 流式文本片段
     */
    public AgentRunResponse(String node, String agent, AssistantMessage message, Usage tokenUsage, String chunk) {
        this.node = node;
        this.agent = agent;
        // 将原生消息转为 DTO 对象，保证序列化正常
        this.message = message   ;
        this.tokenUsage = tokenUsage;
        this.chunk = chunk;
    }

    /**
     * 构造函数：直接通过 MessageDTO 创建响应对象
     * @param node 执行节点
     * @param agent 智能体名称
     * @param message 消息 DTO
     * @param tokenUsage Token 消耗
     * @param chunk 流式文本片段
     */
    public AgentRunResponse(String node, String agent, AbstractMessage message, Usage tokenUsage, String chunk) {
        this.node = node;
        this.agent = agent;
        this.message = message;
        this.tokenUsage = tokenUsage;
        this.chunk = chunk;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public Usage getTokenUsage() {
        return tokenUsage;
    }

    public void setTokenUsage(Usage tokenUsage) {
        this.tokenUsage = tokenUsage;
    }

    public AbstractMessage getMessage() {
        return message;
    }

    public void setMessage(AbstractMessage message) {
        this.message = message;
    }

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String chunk) {
        this.chunk = chunk;
    }
}
