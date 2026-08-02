package org.example.agents.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

public class SearchTool implements BiFunction<String, ToolContext, String> {
    @Override
    public String apply(
            @ToolParam(description = "搜索关键词") String query,
            ToolContext toolContext) {
        return "搜索结果：" + query;
    }
}
