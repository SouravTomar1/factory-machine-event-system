package com.factory.controller;

import com.factory.dto.*;
import com.factory.service.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @PostMapping("/batch")
    public BatchResponse ingest(@RequestBody List<EventRequest> events) {
        return service.ingest(events);
    }
}
