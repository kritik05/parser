package com.parser.Parser.Application.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.ToolType;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private final ElasticsearchClient esClient;

    public ElasticsearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

    private static String computeHash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error computing hash", e);
        }
    }

    public void upsertFinding(Finding newFinding,String indexName) throws IOException {
        if (!doesIndexExist(indexName)) {
            newFinding.setUpdatedAt(Instant.now().toString());
            indexFinding(newFinding,indexName);
            return;
        }
        String alertId = getAlertIdFromAdditionalData(newFinding);
        String firstHash =  computeHash(alertId.trim() + newFinding.getTitle().trim());
        List<Finding> existingSameToolType = findByToolType(newFinding.getToolType(),indexName);
        Optional<Finding> matchedFinding = existingSameToolType.stream()
                .filter(f -> {
                    String fAlertId = getAlertIdFromAdditionalData(f);
                    String fHash = computeHash(fAlertId.trim() + f.getTitle().trim());
                    return fHash.equals(firstHash);
                })
                .findFirst();

        if (matchedFinding.isPresent()) {
            Finding found = matchedFinding.get();
            String oldSeverityStatusHash = computeHash(found.getSeverity() + "-" + found.getStatus());
            String newSeverityStatusHash = computeHash(newFinding.getSeverity() + "-" + newFinding.getStatus());
            if(!oldSeverityStatusHash.equals(newSeverityStatusHash)){
                newFinding.setUpdatedAt(Instant.now().toString());
                newFinding.setId(found.getId());
                indexFinding(newFinding,indexName);
            }
        }
        else{
            newFinding.setUpdatedAt(Instant.now().toString());
            indexFinding(newFinding,indexName);
        }

    }

    public void indexFinding(Finding finding,String indexName) throws IOException {
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

    public List<Finding> findByToolType(ToolType toolType,String indexName) throws IOException {
        try {
            SearchResponse<Finding> response = esClient.search(s -> s
                    .index(indexName)
                    .query(q -> q.term(t -> t
                            .field("toolType.keyword")
                            .value(toolType.name())

                    ))
                    .size(10000),
                    Finding.class
            );
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (ElasticsearchException ex) {
            if (ex.response().error().type().equals("index_not_found_exception")) {
                return Collections.emptyList();
            } else {
                throw ex;
            }
        }
    }

    private String getAlertIdFromAdditionalData(Finding f) {
        if (f.getAdditionalData() != null && f.getAdditionalData().containsKey("number")) {
            Object val = f.getAdditionalData().get("number");
            return val == null ? "" : val.toString();
        }
        return "";
    }
    private boolean doesIndexExist(String indexName) {
        try {
            return esClient.indices().exists(e -> e.index(indexName)).value();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
