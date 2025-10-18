package org.example.springaiali.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pg")
public class PGController {

    @Autowired(required = false)
    PgVectorStore pgVectorStore;


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

    @GetMapping("/delete-filter")
    public void searchFilter() {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression expression = b.and(b.in("year", 2025, 2024), b.eq("name", "yingzi")).build();
//        Filter.Expression expression = b.eq("name", "yingzi").build();

        pgVectorStore.delete(expression);
    }


}
