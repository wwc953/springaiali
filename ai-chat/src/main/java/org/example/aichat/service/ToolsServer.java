package org.example.aichat.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ToolsServer {

    @Tool(description = "获取当前时间")
    public String getCurrTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Tool(description = "获取地区天气情况")
    public String getWeaByCity(@ToolParam(description = "城市") String city) {
        String res = "";
        if ("上海".equals(city)) {
            res = "晴天";
        }
        if ("南京".equals(city)) {
            res = "多云";
        }
        return res;
    }

}
