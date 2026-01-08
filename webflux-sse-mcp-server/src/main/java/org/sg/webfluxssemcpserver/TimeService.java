package org.sg.webfluxssemcpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class TimeService {


    private static final Logger log = LoggerFactory.getLogger(TimeService.class);

    @Tool(description = "根据城市获取天气预报")
    public String getWeatherByCity(@ToolParam(description = "城市") String city) {
        log.info("根据城市获取天气预报 city:{}", city);
        return "当前位置（纬度：39.9042，经度：116.4074）的天气信息：\n" + getWeaByCity(city);
    }

    public String getWeaByCity(String city) {
        log.info("getWeaByCity city:{}", city);
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
