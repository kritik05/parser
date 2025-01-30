package com.parser.Parser.Application.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.parser.Parser.Application.model.Finding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient esClient;

    @Value("${app.elasticsearch.index}")
    private String indexName;

    public ElasticsearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    public void indexFinding(Finding finding) throws IOException {
        if (finding.getId() == null) {
            finding.setId(UUID.randomUUID().toString());
        }

        IndexRequest<Finding> request = IndexRequest.of(i -> i
                .index(indexName)
                .id(finding.getId())
                .document(finding)
        );

        IndexResponse response = esClient.index(request);
    }
}