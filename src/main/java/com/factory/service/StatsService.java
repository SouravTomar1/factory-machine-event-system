package com.factory.service;

import com.factory.dto.StatsResponse;
import com.factory.repository.MachineEventRepository;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class StatsService {

    private final MachineEventRepository repo;

    public StatsService(MachineEventRepository repo) {
        this.repo = repo;
    }

    public StatsResponse stats(String machineId, Instant start, Instant end) {

        long events = repo.countEvents(machineId, start, end);
        long defects = repo.sumDefects(machineId, start, end);

        double hours = Duration.between(start, end).toSeconds() / 3600.0;
        double rate = defects / hours;

        StatsResponse res = new StatsResponse();
        res.machineId = machineId;
        res.start = start;
        res.end = end;
        res.eventsCount = events;
        res.defectsCount = defects;
        res.avgDefectRate = rate;
        res.status = rate < 2.0 ? "Healthy" : "Warning";

        return res;
    }
}
