package org.example.webfluxstreamablemcpserver;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WebfluxStreamableMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxStreamableMcpServerApplication.class, args);
    }
    @Bean
    public ToolCallbackProvider timeTools(AddService addService) {
        return MethodToolCallbackProvider.builder().toolObjects(addService).build();
    }
}
