package com.factory.dto;

import java.time.Instant;

public class EventRequest {
    public String eventId;
    public Instant eventTime;
    public String machineId;
    public long durationMs;
    public int defectCount;
}
