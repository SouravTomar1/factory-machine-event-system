package com.factory;

import com.factory.dto.EventRequest;
import com.factory.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class EventServiceTest {

    @Autowired
    EventService service;

    @Test
    void duplicateShouldDedup() {

        EventRequest e = new EventRequest();
        e.eventId = "E1";
        e.eventTime = Instant.now();
        e.machineId = "M1";
        e.durationMs = 1000;
        e.defectCount = 0;

        service.ingest(List.of(e));
        var r = service.ingest(List.of(e));

        assertEquals(1, r.deduped);
    }
}
