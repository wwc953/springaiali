package org.example.springaiali.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.elasticsearch.ElasticsearchVectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/es")
public class ESController {

    @Autowired(required = false)
    ElasticsearchVectorStore elasticsearchVectorStore;


    @GetMapping("/import")
    public void importData() {
        log.info("start import data");

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", "12345");
        map.put("year", "2025");
        map.put("name", "yingzi");

        List<Document> documents = List.of(
                new Document("世界很大，救赎就在眼前"),
                new Document("你面向过去向前走，然后又转身面向未来。", Map.of("year", 2024)),
                new Document("Spring AI 太棒了！！Spring AI 太棒了！！Spring AI 太棒了！！", map));
        elasticsearchVectorStore.add(documents);
    }

    @GetMapping("/search/{str}")
    public List<Document> search(@PathVariable String str) {
        if (StringUtils.isBlank(str)) {
            str = "Spring";
        }
        log.info("start search data: {}", str);
        return elasticsearchVectorStore.similaritySearch(SearchRequest
                .builder()
                .query(str)
                .topK(2)
                .build());
    }

    @GetMapping("/delete-filter")
    public void searchFilter() {
        FilterExpressionBuilder b = new FilterExpressionBuilder();
//        Filter.Expression expression = b.and(
//                b.in("year", 2025, 2024),
//                b.eq("name", "yingzi")
//        ).build();
        Filter.Expression expression = b.eq("name", "yingzi").build();

        elasticsearchVectorStore.delete(expression);
    }


}
