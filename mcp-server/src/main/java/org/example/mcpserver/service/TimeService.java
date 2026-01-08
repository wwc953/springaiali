package org.example.mcpserver.service;

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

    @Tool(description = "根据经纬度获取天气预报")
    public String getWeatherForecastByLocation(
            @ToolParam(description = "纬度，例如：39.9042") String latitude,
            @ToolParam(description = "经度，例如：116.4074") String longitude) {
        try {
//            String response = webClient.get()
//                    .uri(uriBuilder -> uriBuilder
//                            .path("/forecast")
//                            .queryParam("latitude", latitude)
//                            .queryParam("longitude", longitude)
//                            .queryParam("current", "temperature_2m,wind_speed_10m")
//                            .queryParam("timezone", "auto")
//                            .build())
//                    .retrieve()
//                    .bodyToMono(String.class)
//                    .block();

            // 解析响应并返回格式化的天气信息
            // 这里简化处理，实际应用中应该解析JSON
            return "当前位置（纬度：" + latitude + "，经度：" + longitude + "）的天气信息：\n";
        } catch (Exception e) {
            return "获取天气信息失败：" + e.getMessage();
        }
    }

    @Tool(description = "根据经纬度获取空气质量信息")
    public String getAirQuality(
            @ToolParam(description = "纬度，例如：39.9042") String latitude,
            @ToolParam(description = "经度，例如：116.4074") String longitude) {

        // 模拟数据，实际应用中应调用真实API
        return "当前位置（纬度：" + latitude + "，经度：" + longitude + "）的空气质量：\n" +
                "- PM2.5: 15 μg/m³ (优)\n" +
                "- PM10: 28 μg/m³ (良)\n" +
                "- 空气质量指数(AQI): 42 (优)\n" +
                "- 主要污染物: 无";
    }

//    @Tool(description = "获取当前时间")
//    public String getCurrTime() {
//        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        log.info("getCurrTime time:{}", time);
//        return time;
//    }

//    @Tool(description = "获取地区天气情况")
//    public String getWeaByCity(@ToolParam(description = "城市") String city) {
//        log.info("getWeaByCity city:{}", city);
//        String res = "";
//        if ("上海".equals(city)) {
//            res = "晴天";
//        }
//        if ("南京".equals(city)) {
//            res = "多云";
//        }
//        return res;
//    }

}
