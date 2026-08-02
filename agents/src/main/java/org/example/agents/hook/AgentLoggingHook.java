package org.example.agents.hook;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.agent.hook.AgentHook;
import com.alibaba.cloud.ai.graph.agent.hook.HookPosition;
import com.alibaba.cloud.ai.graph.agent.hook.HookPositions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
// 1. AgentHook - 在 Agent 开始/结束时执行，每次Agent调用只会运行一次
@HookPositions({HookPosition.BEFORE_AGENT, HookPosition.AFTER_AGENT})  //等同重写getHookPositions()
public class AgentLoggingHook extends AgentHook {

    @Override
    public String getName() {
        return "AgentLogging";
    }

    @Override
    public CompletableFuture<Map<String, Object>> beforeAgent(OverAllState state, RunnableConfig config) {
        log.info("Agent 开始执行");
        return CompletableFuture.completedFuture(Map.of());
    }

    @Override
    public CompletableFuture<Map<String, Object>> afterAgent(OverAllState state, RunnableConfig config) {
        log.info("Agent 执行完成");
        return CompletableFuture.completedFuture(Map.of());
    }

    //- 在 Agent 开始/结束时执行，每次Agent调用只会运行一次
//    @Override
//    public HookPosition[] getHookPositions() {
//        return new HookPosition[]{
//                HookPosition.BEFORE_AGENT,
//                HookPosition.AFTER_AGENT
//        };
//    }
}
