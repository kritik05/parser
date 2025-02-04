package com.parser.Parser.Application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.ToolType;
import com.parser.Parser.Application.service.mappers.CodeScanMapper;
import com.parser.Parser.Application.service.mappers.DependabotMapper;
import com.parser.Parser.Application.service.mappers.SecretScanMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ParserService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CodeScanMapper codeScanMapper = new CodeScanMapper();
    private final DependabotMapper dependabotMapper = new DependabotMapper();
    private final SecretScanMapper secretScanMapper = new SecretScanMapper();

    public List<Finding> parse(ToolType toolType, String rawJson) {
        switch (toolType) {
            case CODESCAN:
                return parseCodeScan(rawJson);
            case DEPENDABOT:
                return parseDependabot(rawJson);
            case SECRETSCAN:
                return parseSecretScan(rawJson);
            default:
                return Collections.emptyList();
        }
    }

    private List<Finding> parseCodeScan(String rawJson) {
        List<Finding> findings = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            if (root.isArray()) {
                for (JsonNode alert : root) {
                    findings.add(buildFindingFromCodeScan(alert));
                }
            } else {
                findings.add(buildFindingFromCodeScan(root));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return findings;
    }

    private Finding buildFindingFromCodeScan(JsonNode node) {
        Finding f = new Finding();
        f.setToolType(ToolType.CODESCAN);

        f.setId(UUID.randomUUID().toString());

        JsonNode rule = node.path("rule");
        f.setTitle(rule.path("name").asText("Unnamed CodeScan Alert"));

        f.setDescription(rule.path("full_description").asText(""));

        String rawState = node.path("state").asText("open");
        f.setStatus(codeScanMapper.mapStatus(rawState));

        String rawSev = rule.path("security_severity_level").asText("medium");
        f.setSeverity(codeScanMapper.mapSeverity(rawSev));

        String cve = node.path("cve").asText("");
        f.setCve(cve);

        double cvssScore = node.path("cvss").asDouble(0.0);
        f.setCvss(cvssScore);

        f.setUrl(node.path("html_url").asText(""));

        List<String> cweList = new ArrayList<>();
        JsonNode tags = rule.path("tags");
        if (tags.isArray()) {
            for (JsonNode t : tags) {
                if (t.asText().contains("cwe/")) {
                    cweList.add(t.asText());
                }
            }
        }
        if (!cweList.isEmpty()) {
            f.setCwe(String.join(",", cweList));
        }

        JsonNode loc = node.path("most_recent_instance").path("location");
        if (!loc.isMissingNode()) {
            String path = loc.path("path").asText("");
            int startLine = loc.path("start_line").asInt(-1);
            f.setLocation(path + " (line " + startLine + ")");
        }

        Map<String, Object> leftover = objectMapper.convertValue(node, Map.class);
        f.setAdditionalData(leftover);

        return f;
    }

    private List<Finding> parseDependabot(String rawJson) {
        List<Finding> findings = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            if (root.isArray()) {
                for (JsonNode alert : root) {
                    findings.add(buildFindingFromDependabot(alert));
                }
            } else {
                findings.add(buildFindingFromDependabot(root));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return findings;
    }

    private Finding buildFindingFromDependabot(JsonNode node) {
        Finding f = new Finding();
        f.setToolType(ToolType.DEPENDABOT);

        f.setId(UUID.randomUUID().toString());

        JsonNode advisory = node.path("security_advisory");
        f.setTitle(advisory.path("summary").asText("Unnamed Dependabot Alert"));
        f.setDescription(advisory.path("description").asText(""));

        String rawState = node.path("state").asText("open");
        f.setStatus(dependabotMapper.mapStatus(rawState));

        String rawSev = advisory.path("severity").asText("medium");
        f.setSeverity(dependabotMapper.mapSeverity(rawSev));

        f.setUrl(node.path("html_url").asText(""));

        f.setCve(advisory.path("cve_id").asText(""));

        JsonNode cwes = advisory.path("cwes");
        if (cwes.isArray() && cwes.size() > 0) {
            JsonNode first = cwes.get(0);
            f.setCwe(first.path("cwe_id").asText(""));
        }

        double cvssScore = advisory.path("cvss").path("score").asDouble(0.0);
        f.setCvss(cvssScore);

        String manifestPath = node.path("dependency").path("manifest_path").asText("");
        f.setLocation(manifestPath);

        Map<String, Object> leftover = objectMapper.convertValue(node, Map.class);
        f.setAdditionalData(leftover);

        f.setAdditionalData(leftover);

        return f;
    }


    private List<Finding> parseSecretScan(String rawJson) {
        List<Finding> findings = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            if (root.isArray()) {
                for (JsonNode alert : root) {
                    findings.add(buildFindingFromSecretScan(alert));
                }
            } else {
                findings.add(buildFindingFromSecretScan(root));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return findings;
    }

    private Finding buildFindingFromSecretScan(JsonNode node) {
        Finding f = new Finding();
        f.setToolType(ToolType.SECRETSCAN);

        f.setId(UUID.randomUUID().toString());

        f.setTitle(node.path("secret_type_display_name").asText("Secret Alert"));

        f.setDescription("Exposed secret of type: " + node.path("secret_type").asText(""));

        String rawState = node.path("state").asText("open");
        f.setStatus(secretScanMapper.mapStatus(rawState));

        f.setSeverity(secretScanMapper.mapSeverity(null));

        f.setUrl(node.path("html_url").asText(""));

        f.setCve("");
        f.setCwe("");
        f.setCvss(0.0);

        f.setLocation("");

        Map<String, Object> leftover = objectMapper.convertValue(node, Map.class);
        f.setAdditionalData(leftover);

        f.setAdditionalData(leftover);

        return f;
    }

    private Instant parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return Instant.now();
        }
        try {
            return Instant.parse(dateStr);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
