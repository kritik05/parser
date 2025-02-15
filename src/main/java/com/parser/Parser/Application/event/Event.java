package com.parser.Parser.Application.event;

public interface Event <T>{
    String getType();
    T getPayload();
    String getEventId();
}
