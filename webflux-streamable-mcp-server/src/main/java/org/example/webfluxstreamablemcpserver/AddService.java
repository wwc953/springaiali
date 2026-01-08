package org.example.webfluxstreamablemcpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AddService {


    private static final Logger log = LoggerFactory.getLogger(AddService.class);

    @Tool(description = "计算两个整数数的和")
    public Integer getInt1AddInt2(@ToolParam(description = "第一个整数") Integer int1, @ToolParam(description = "第二个整数") Integer int2) {
        log.info("计算两个整数数的和 int1:{} int2:{}", int1, int2);
        return int1 + int2;
    }


    @Tool(description = "获取当前时间")
    public String getCurrTime() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("getCurrTime time:{}", time);
        return time;
    }

}
