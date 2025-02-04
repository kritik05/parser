package com.parser.Parser.Application.service.mappers;

import com.parser.Parser.Application.model.Severity;
import com.parser.Parser.Application.model.Status;

public class SecretScanMapper {

    public Status mapStatus(String rawState) {
        if (rawState == null) {
            return Status.OPEN;
        }
        switch (rawState.toLowerCase()) {
            case "open":
                return Status.OPEN;
            case "resolved":
                return Status.FIXED;
            default:
                return Status.OPEN;
        }
    }

    public Severity mapSeverity(String rawSeverity) {
        return Severity.CRITICAL;

    }
}