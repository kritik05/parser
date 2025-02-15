package com.parser.Parser.Application.event;

public interface Acknowledgment<T> {
    String getAcknowledgementId();
    T getPayload();
}
