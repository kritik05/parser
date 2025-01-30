package com.parser.Parser.Application.model;

import java.time.Instant;
import java.util.Map;

public class Finding {

    private String id;             // could be from the tool's "number" or a random UUID
    private ToolType toolType;     // code scanning, dependabot, or secret scanning
    private String title;
    private String description;
    private Status status;
    private Severity severity;
//    private Instant createdAt;
//    private Instant updatedAt;
    private String url;
    private String cve;
    private String cwe;
    private Double cvss;
    private String location;       // e.g. file path or dependency location
    private Map<String, Object> additionalData; // leftover fields

    public Finding() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ToolType getToolType() {
        return toolType;
    }

    public void setToolType(ToolType toolType) {
        this.toolType = toolType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

//    public Instant getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(Instant createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public Instant getUpdatedAt() {
//        return updatedAt;
//    }
//
//    public void setUpdatedAt(Instant updatedAt) {
//        this.updatedAt = updatedAt;
//    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCve() {
        return cve;
    }

    public void setCve(String cve) {
        this.cve = cve;
    }

    public String getCwe() {
        return cwe;
    }

    public void setCwe(String cwe) {
        this.cwe = cwe;
    }

    public Double getCvss() {
        return cvss;
    }

    public void setCvss(Double cvss) {
        this.cvss = cvss;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Map<String, Object> additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public String toString() {
        return "Finding{" +
                "id='" + id + '\'' +
                ", toolType=" + toolType +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", severity=" + severity +
//                ", createdAt=" + createdAt +
//                ", updatedAt=" + updatedAt +
                ", url='" + url + '\'' +
                ", cve='" + cve + '\'' +
                ", cwe='" + cwe + '\'' +
                ", cvss=" + cvss +
                ", location='" + location + '\'' +
                ", additionalData=" + additionalData +
                '}';
    }
}