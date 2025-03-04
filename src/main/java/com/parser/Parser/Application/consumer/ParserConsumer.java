package com.parser.Parser.Application.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parser.Parser.Application.event.AcknowledgementEvent;
import com.parser.Parser.Application.event.ParseRequestEvent;
import com.parser.Parser.Application.event.RunbookRequestEvent;
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
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ParserConsumer {
    private final TenantRepository tenantRepository;
    private final ParserService parserService;
    private final ElasticsearchService elasticsearchService;
    private final KafkaTemplate<String, Object> ackTemplate;
    private final KafkaTemplate<String, String> sendingJob;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    @Value("${app.kafka.topics.parser}")
    private String topic;

    @Value("${app.kafka.topics.ack}")
    private String ackTopic;


    @Value("${app.kafka.topics.jfcunified}")
    private String unifiedTopic;

    public ParserConsumer(ParserService parserService, ElasticsearchService elasticsearchService,TenantRepository tenantRepository,  KafkaTemplate<String, Object> ackTemplate, KafkaTemplate<String, String> sendingJob, ObjectMapper objectMapper) {
        this.parserService = parserService;
        this.elasticsearchService = elasticsearchService;
        this.tenantRepository=tenantRepository;
        this.ackTemplate=ackTemplate;
        this.sendingJob=sendingJob;
        this.objectMapper=objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.parser}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "parseRequestEventListenerContainerFactory"
    )
    public void consumeParseRequestEvent(ParseRequestEvent event) throws JsonProcessingException {
        String originalEventId = event.getEventId();
        try{
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


            String rawJson = Files.readString(file.toPath());
            ToolType toolType = mapToolType(tooltype);
            List<Finding> findings = parserService.parse(toolType, rawJson);

            String findingIndex = tenant.getFindingindex();
            List<String> findingIds = new ArrayList<>();

            for (Finding f : findings) {
                String id=elasticsearchService.upsertFinding(f,findingIndex);
                if(id!="") findingIds.add(id);
                System.out.println("Indexed finding with ID=" + id);
            }


            AcknowledgementPayload ackPayload = new AcknowledgementPayload(originalEventId, "SUCCESS");
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
            ackTemplate.send(ackTopic, ackEvent);
            System.out.println("sent ack from parser");

            int sleepMs = 3000 + random.nextInt(3000);
            Thread.sleep(sleepMs);



            RunbookPayload runbookPayload = new RunbookPayload(
                            null,
                            tenantId,
                            findingIds,
                            "SCAN_EVENT"
                    );

                    RunbookRequestEvent runbookRequestEvent = new RunbookRequestEvent(runbookPayload);
                    String json = objectMapper.writeValueAsString(runbookRequestEvent);
                    sendingJob.send(unifiedTopic, json);
            System.out.println(runbookRequestEvent);
                    System.out.println("Sent RunbookEvent for tenant=" + tenantId);

        } catch (IOException e) {
            AcknowledgementPayload ackPayload = new AcknowledgementPayload(originalEventId, "FAIL");
            AcknowledgementEvent ackEvent = new AcknowledgementEvent(null, ackPayload);
            ackTemplate.send(ackTopic, ackEvent);
            System.out.println("sent ack from parser");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
