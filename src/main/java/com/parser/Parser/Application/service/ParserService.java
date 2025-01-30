package com.parser.Parser.Application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parser.Parser.Application.model.Finding;
import com.parser.Parser.Application.model.Severity;
import com.parser.Parser.Application.model.Status;
import com.parser.Parser.Application.model.ToolType;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ParserService {

    private final ObjectMapper objectMapper = new ObjectMapper();

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

        // ID from "number"
        f.setId(node.path("number").asText(UUID.randomUUID().toString()));

        // Title from "rule.name" or fallback
        JsonNode rule = node.path("rule");
        f.setTitle(rule.path("name").asText("Unnamed CodeScan Alert"));

        // Description from "rule.description"
        f.setDescription(rule.path("full_description").asText(""));

        // Status from "state"
        String rawState = node.path("state").asText("open");
        f.setStatus(mapStatus(rawState));

        // Severity from "rule.security_severity_level"
        String rawSev = rule.path("security_severity_level").asText("medium");
        f.setSeverity(mapSeverity(rawSev));

        // Handle optional CVE if present
        // Adjust the path to wherever CVE might actually appear in your data
        String cve = node.path("cve").asText("");
        f.setCve(cve);

        // Handle optional CVSS score if present
        // Adjust the path to wherever CVSS might actually appear in your data
        double cvssScore = node.path("cvss").asDouble(0.0);
        f.setCvss(cvssScore);

        // url = "html_url"
        f.setUrl(node.path("html_url").asText(""));

        // parse cwe from "rule.tags" if it contains something like "external/cwe/cwe-073"
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

        // location => node.most_recent_instance.location.path + line
        JsonNode loc = node.path("most_recent_instance").path("location");
        if (!loc.isMissingNode()) {
            String path = loc.path("path").asText("");
            int startLine = loc.path("start_line").asInt(-1);
            f.setLocation(path + " (line " + startLine + ")");
        }

        // leftover in additionalData:
        Map<String, Object> leftover = objectMapper.convertValue(node, Map.class);
//        leftover.put("toolName", node.path("tool").path("name").asText(""));
//        leftover.put("dismissed_reason", node.path("dismissed_reason").asText(""));
//        leftover.put("fixed_at", node.path("fixed_at").asText(""));

        // Add the entire original JSON node as a string for reference
//        leftover.put("complete_data", node.toString());

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

        // ID from "number"
        f.setId(node.path("number").asText(UUID.randomUUID().toString()));

        // Title from "security_advisory.summary"
        JsonNode advisory = node.path("security_advisory");
        f.setTitle(advisory.path("summary").asText("Unnamed Dependabot Alert"));
        f.setDescription(advisory.path("description").asText(""));

        // status from "state": "open", "dismissed", etc.
        f.setStatus(mapStatus(node.path("state").asText("open")));

        // severity from "security_advisory.severity"
        String rawSev = advisory.path("severity").asText("medium");
        f.setSeverity(mapSeverity(rawSev));

        // url => "html_url"
        f.setUrl(node.path("html_url").asText(""));

        // cve => "security_advisory.cve_id"
        f.setCve(advisory.path("cve_id").asText(""));

        // cwe => from "cwes"[0].cwe_id if present
        JsonNode cwes = advisory.path("cwes");
        if (cwes.isArray() && cwes.size() > 0) {
            JsonNode first = cwes.get(0);
            f.setCwe(first.path("cwe_id").asText(""));
        }

        // cvss => from "security_advisory.cvss.score"
        double cvssScore = advisory.path("cvss").path("score").asDouble(0.0);
        f.setCvss(cvssScore);

        // location => "dependency.manifest_path"
        String manifestPath = node.path("dependency").path("manifest_path").asText("");
        f.setLocation(manifestPath);

        // leftover
        Map<String, Object> leftover = new HashMap<>();
//        leftover.put("dismissed_reason", node.path("dismissed_reason").asText(""));
//        leftover.put("security_vulnerability", node.path("security_vulnerability").toString());
        // Add the entire original JSON node as a string for reference
        leftover.put("complete_data", node.toString());

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

        // id => "number"
        f.setId(node.path("number").asText(UUID.randomUUID().toString()));

        // title => "secret_type_display_name"
        f.setTitle(node.path("secret_type_display_name").asText("Secret Alert"));

        // description => "secret_type"
        f.setDescription("Exposed secret of type: " + node.path("secret_type").asText(""));

        // status from "state"
        f.setStatus(mapStatus(node.path("state").asText("open")));

        // severity? fallback to MEDIUM unless you have some logic to adjust
        f.setSeverity(Severity.CRITICAL);

        // url => "html_url"
        f.setUrl(node.path("html_url").asText(""));

        // cve, cwe => not typical in secrets
        f.setCve("");
        f.setCwe("");

        // no CVSS
        f.setCvss(0.0);

        // location => optional
        f.setLocation("");

        // leftover
        Map<String, Object> leftover = new HashMap<>();
//        leftover.put("secret", node.path("secret").asText(""));
//        leftover.put("validity", node.path("validity").asText(""));
//        leftover.put("publicly_leaked", node.path("publicly_leaked").asBoolean(false));
        // Add the entire original JSON node as a string for reference
        leftover.put("complete_data", node.toString());

        f.setAdditionalData(leftover);

        return f;
    }

    /**
     * Map raw string from GitHub (like "open", "dismissed", "fixed") to our Status enum.
     */
    private Status mapStatus(String rawState) {
        if (rawState == null) {
            return Status.OPEN;
        }
        switch (rawState.toLowerCase()) {
            case "open":
                return Status.OPEN;
            case "auto_dismissed":
                return Status.SUPPRESSED;
            case "dismissed":
                return Status.FALSE_POSITIVE;
            case "fixed":
            case "resolved":
            case "closed":
                return Status.FIXED;
            case "confirmed":
                return Status.CONFIRM;
            default:
                return Status.OPEN;
        }
    }

    /**
     * Map raw severity string to our Severity enum, which includes SEVERE, CRITICAL, HIGH, MEDIUM, LOW, INFO.
     */
    private Severity mapSeverity(String rawSev) {
        if (rawSev == null) {
            return Severity.INFO;
        }
        switch (rawSev.toLowerCase()) {
            case "critical":
                return Severity.CRITICAL;
            case "high":
            case "error":
                return Severity.HIGH;
            case "medium":
            case "moderate":
            case "warning":
                return Severity.MEDIUM;
            case "low":
            case "note":
                return Severity.LOW;
            default:
                return Severity.INFO;
        }
    }

    /**
     * Parse a date string like "2025-01-27T05:06:14Z" into Instant.
     */
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
