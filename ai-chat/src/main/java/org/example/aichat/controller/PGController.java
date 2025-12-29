package org.example.aichat.controller;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.example.aichat.service.ToolsServer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pg")
public class PGController {


    @Resource
    PgVectorStore pgVectorStore;


    @Resource
    ChatClient chatClient;

    @Resource
    ToolsServer toolsServer;

    @Resource
    ConfigurablePromptTemplateFactory configurablePromptTemplateFactory;

    /**
     * ChatClient 使用自定义的 Advisor 实现功能增强.
     * eg:
     * http://127.0.0.1:18080/pg/advisor/chat/123?query=你好，我叫jack，之后的会话中都带上我的名字
     * 你好，jack！很高兴认识你。在接下来的对话中，我会记得带上你的名字。有什么想聊的吗？
     * http://127.0.0.1:18080/pg/advisor/chat/123?query=我叫什么名字？
     * 你叫jack呀。有什么事情想要分享或者讨论吗，jack？
     * <p>
     * refer: https://docs.spring.io/spring-ai/reference/api/chat-memory.html#_memory_in_chat_client
     */
    @GetMapping("/advisor/chat/{conversationId}")
    public Flux<String> advisorChat(@PathVariable String conversationId,
                                    @RequestParam String query,
                                    HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        ConfigurablePromptTemplate template = configurablePromptTemplateFactory.create("test-template", "");
        Prompt prompt = null;
        if (StringUtils.isNotBlank(query)) {
            prompt = template.create(Map.of("author", query));
        } else {
            prompt = template.create();
        }
        log.info("prompt===>{}", prompt.getContents());
        return chatClient.prompt(prompt)
                .tools(toolsServer)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream().content();
    }


    @GetMapping("/import")
    public void importData() {
        log.info("start import data");
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", "12345");
        map.put("year", "2025");
        map.put("name", "yingzi");

        List<Document> documents = List.of(new Document("世界很大，救赎就在眼前"),
                new Document("你面向过去向前走，然后又转身面向未来。", Map.of("year", 2024)),
                new Document("Spring AI 太棒了！！Spring AI 太棒了！！Spring AI 太棒了！！", map));
        pgVectorStore.add(documents);
    }

    @GetMapping("/search/{query}")
    public List<Document> search(@PathVariable String query, @RequestParam(required = false, defaultValue = "0.0") Double threshold, @RequestParam(required = false, defaultValue = "2") Integer topK) {
        log.info("start search threshold {}, topK:{} , query: {}", threshold, topK, query);
        SearchRequest.Builder sbuild = SearchRequest.builder();
        if (threshold != null) {
            sbuild.similarityThreshold(threshold);//介于 0 到 1 之间的双精度值，值越接近 1，相似度越高。默认情况下，例如，如果您将阈值设置为 0.75，则仅返回相似度高于此值的文档
        }
        SearchRequest searchRequest = sbuild.query(query).topK(topK)//K 个最近邻
                .build();
        return pgVectorStore.similaritySearch(searchRequest);
    }


    @GetMapping("/search/filter/{query}")
    public List<Document> searchF(@PathVariable String query) {
        log.info("start search  filter : {}", query);
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(2)
                .filterExpression("year in [2025, 2024] && name == 'yingzi'")
                .build();
        return pgVectorStore.similaritySearch(searchRequest);
    }

//    @GetMapping("/delete-filter")
//    public void searchFilter() {
//        FilterExpressionBuilder b = new FilterExpressionBuilder();
//        Filter.Expression expression = b.and(b.in("year", 2025, 2024), b.eq("name", "yingzi")).build();
////        Filter.Expression expression = b.eq("name", "yingzi").build();
//        pgVectorStore.delete(expression);
//    }


//    @Value("classpath:doc/code.md")
//    Resource resource;
//
//    /**
//     * 导入md文件
//     */
//    @GetMapping("/importMd")
//    public void importMd() {
//        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
//                .withHorizontalRuleCreateDocument(true)
//                .withIncludeCodeBlock(false)
//                .withIncludeBlockquote(false)
//                .withAdditionalMetadata("filename", "code.md")
//                .build();
//        MarkdownDocumentReader reader = new MarkdownDocumentReader(this.resource, config);
//        List<Document> documents = reader.get();
//        pgVectorStore.add(documents);
//    }

}
