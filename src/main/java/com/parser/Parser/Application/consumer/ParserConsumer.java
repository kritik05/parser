package com.parser.Parser.Application.consumer;

import com.parser.Parser.Application.model.FileLocationEvent;
import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.ToolType;
import com.parser.Parser.Application.service.ElasticsearchService;
import com.parser.Parser.Application.service.ParserService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Component
public class ParserConsumer {

    private final ParserService parserService;
    private final ElasticsearchService elasticsearchService;

    @Value("${app.kafka.topics.filelocation}")
    private String fileLocationTopic;

    public ParserConsumer(ParserService parserService, ElasticsearchService elasticsearchService) {
        this.parserService = parserService;
        this.elasticsearchService = elasticsearchService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.filelocation}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "fileLocationEventListenerFactory"
    )
    public void consumeFileLocationEvent(ConsumerRecord<String, FileLocationEvent> record) {
        FileLocationEvent fle = record.value();
        System.out.println("Received FileLocationEvent: " + fle);

        File file = new File(fle.getFilePath());
        if (!file.exists()) {
            System.err.println("File not found at path: " + fle.getFilePath());
            return;
        }

        try {
            String rawJson = Files.readString(file.toPath());
            ToolType toolType = mapToolType(fle.getToolName());
            List<Finding> findings = parserService.parse(toolType, rawJson);

            String findingIndex = fle.getFindingindex();
            for (Finding f : findings) {
                elasticsearchService.upsertFinding(f,findingIndex);
                System.out.println("Indexed finding with ID=" + f.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ToolType mapToolType(String toolName) {
        if (toolName == null) {
            return ToolType.CODESCAN;
        }
        switch (toolName.toLowerCase()) {
            case "codescan":
                return ToolType.CODESCAN;
            case "dependabot":
                return ToolType.DEPENDABOT;
            case "secretscan":
                return ToolType.SECRETSCAN;
            default:
                return ToolType.CODESCAN;
        }
    }
}
