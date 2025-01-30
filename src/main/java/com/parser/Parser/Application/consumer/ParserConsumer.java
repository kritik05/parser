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

        // 1) read the file from fle.getFilePath()
        File file = new File(fle.getFilePath());
        if (!file.exists()) {
            System.err.println("File not found at path: " + fle.getFilePath());
            return;
        }

        try {
            // read raw JSON as a string
            String rawJson = Files.readString(file.toPath());

            // 2) convert fle.getToolName() to our ToolType enum
            ToolType toolType = mapToolType(fle.getToolName());

            // 3) parse it
            List<Finding> findings = parserService.parse(toolType, rawJson);

            // 4) index each
            for (Finding f : findings) {
                elasticsearchService.indexFinding(f);
                System.out.println("Indexed finding with ID=" + f.getId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Convert string from Tool Scheduler ("codescan", "dependabot", "secretscan") to ToolType enum
    private ToolType mapToolType(String toolName) {
        if (toolName == null) {
            return ToolType.CODESCAN; // fallback
        }
        switch (toolName.toLowerCase()) {
            case "codescan":
                return ToolType.CODESCAN;
            case "dependabot":
                return ToolType.DEPENDABOT;
            case "secretscan":
                return ToolType.SECRETSCAN;
            default:
                return ToolType.CODESCAN; // fallback
        }
    }
}
