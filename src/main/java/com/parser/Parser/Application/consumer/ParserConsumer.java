package com.parser.Parser.Application.consumer;

import com.parser.Parser.Application.event.AcknowledgementEvent;
import com.parser.Parser.Application.event.ParseRequestEvent;
import com.parser.Parser.Application.model.*;
import com.parser.Parser.Application.repository.TenantRepository;
import com.parser.Parser.Application.service.ElasticsearchService;
import com.parser.Parser.Application.service.ParserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@Component
public class ParserConsumer {
    private final TenantRepository tenantRepository;
    private final ParserService parserService;
    private final ElasticsearchService elasticsearchService;
    private final KafkaTemplate<String, Object> ackTemplate;

    @Value("${app.kafka.topics.parser}")
    private String topic;

    @Value("${app.kafka.topics.ack}")
    private String ackTopic;

    public ParserConsumer(ParserService parserService, ElasticsearchService elasticsearchService,TenantRepository tenantRepository,  KafkaTemplate<String, Object> ackTemplate) {
        this.parserService = parserService;
        this.elasticsearchService = elasticsearchService;
        this.tenantRepository=tenantRepository;
        this.ackTemplate=ackTemplate;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.parser}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "parseRequestEventListenerContainerFactory"
    )
    public void consumeParseRequestEvent(ParseRequestEvent event) {
        String filePath = event.getPayload().getFilePath();
        String tooltype = event.getPayload().getTooltype();
        Integer tenantId = event.getPayload().getTenantId();
        Optional<Tenant> optionalTenant = tenantRepository.findById(tenantId);
        if (optionalTenant.isEmpty()) {
            return;
        }
        Tenant tenant = optionalTenant.get();
        if(filePath==null){
            System.out.println("file path doesnt exist");
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File not found at path: " + filePath);
            return;
        }

        try {
            String rawJson = Files.readString(file.toPath());
            ToolType toolType = mapToolType(tooltype);
            List<Finding> findings = parserService.parse(toolType, rawJson);

            String findingIndex = tenant.getFindingindex();
            for (Finding f : findings) {
                elasticsearchService.upsertFinding(f,findingIndex);
                System.out.println("Indexed finding with ID=" + f.getId());
            }

            String originalEventId = event.getEventId();
            AcknowledgementPayload ackPayload = new AcknowledgementPayload(originalEventId, "SUCCESS");
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
            ackTemplate.send(ackTopic, ackEvent);
            System.out.println("sent ack from parser");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ToolType mapToolType(String toolName) {
        if (toolName == null) {
            return ToolType.CODESCAN;
        }
        switch (toolName) {
            case "CODESCAN":
                return ToolType.CODESCAN;
            case "DEPENDABOT":
                return ToolType.DEPENDABOT;
            case "SECRETSCAN":
                return ToolType.SECRETSCAN;
            default:
                return ToolType.CODESCAN;
        }
    }
}
