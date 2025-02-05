package com.parser.Parser.Application.service.mappers;

import com.parser.Parser.Application.model.Severity;
import com.parser.Parser.Application.model.Status;

public class CodeScanMapper {

    public Status mapStatus(String rawState,String dismissedReason) {
        if (rawState == null) {
            return Status.OPEN;
        }
        String lowerState = rawState.toLowerCase();
        String lowerReason = dismissedReason == null ? "" : dismissedReason.toLowerCase();
        switch (lowerState) {
            case "open":
                return Status.OPEN;

            case "dismissed":
                switch (lowerReason) {
                    case "false positive":
                        return Status.FALSE_POSITIVE;
                    case "won't fix":
                        return Status.SUPPRESSED;
                    case "used in tests":
                        return Status.SUPPRESSED;
                    default:
                        return Status.FALSE_POSITIVE;
                }

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