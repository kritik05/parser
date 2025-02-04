package com.parser.Parser.Application.service.mappers;

import com.parser.Parser.Application.model.Severity;
import com.parser.Parser.Application.model.Status;

public class CodeScanMapper {

    public Status mapStatus(String rawState) {
        if (rawState == null) {
            return Status.OPEN;
        }
        switch (rawState.toLowerCase()) {
            case "open":
                return Status.OPEN;
            case "dismissed":
                return Status.FALSE_POSITIVE;
            case "fixed":
            case "closed":
                return Status.FIXED;
            default:
                return Status.OPEN;
        }
    }

    public Severity mapSeverity(String rawSeverity) {
        if (rawSeverity == null) {
            return Severity.INFO;
        }
        switch (rawSeverity.toLowerCase()) {
            case "critical":
                return Severity.CRITICAL;
            case "high":
            case "error":
                return Severity.HIGH;
            case "medium":
            case "warning":
                return Severity.MEDIUM;
            case "low":
            case "note":
                return Severity.LOW;
            default:
                return Severity.INFO;
        }
    }
}