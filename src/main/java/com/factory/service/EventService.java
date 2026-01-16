package com.factory.service;

import com.factory.dto.*;
import com.factory.model.MachineEvent;
import com.factory.repository.MachineEventRepository;
import com.factory.util.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class EventService {

    private final MachineEventRepository repo;

    public EventService(MachineEventRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public synchronized BatchResponse ingest(List<EventRequest> events) {

        BatchResponse res = new BatchResponse();
        Instant now = Instant.now();

        for (EventRequest req : events) {

            if (req.durationMs < 0 || req.durationMs > 21600000) {
                reject(res, req.eventId, "INVALID_DURATION");
                continue;
            }

            if (req.eventTime.isAfter(now.plusSeconds(900))) {
                reject(res, req.eventId, "FUTURE_EVENT");
                continue;
            }

            String hash = HashUtil.hash(req);

            Optional<MachineEvent> existingOpt = repo.findByEventId(req.eventId);

            if (existingOpt.isPresent()) {
                MachineEvent old = existingOpt.get();

                if (old.getPayloadHash().equals(hash)) {
                    res.deduped++;
                    continue;
                }

                if (now.isAfter(old.getReceivedTime())) {
                    update(old, req, hash, now);
                    repo.save(old);
                    res.updated++;
                } else {
                    res.deduped++;
                }

            } else {
                MachineEvent ev = new MachineEvent();
                update(ev, req, hash, now);
                repo.save(ev);
                res.accepted++;
            }
        }
        return res;
    }

    private void update(MachineEvent e, EventRequest r, String hash, Instant now) {
        e.setEventId(r.eventId);
        e.setEventTime(r.eventTime);
        e.setMachineId(r.machineId);
        e.setDurationMs(r.durationMs);
        e.setDefectCount(r.defectCount);
        e.setPayloadHash(hash);
        e.setReceivedTime(now);
    }

    private void reject(BatchResponse r, String id, String reason) {
        r.rejected++;
        r.rejections.add(Map.of("eventId", id, "reason", reason));
    }
}
