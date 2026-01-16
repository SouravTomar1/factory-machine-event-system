package com.factory.repository;

import com.factory.model.MachineEvent;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface MachineEventRepository extends JpaRepository<MachineEvent, Long> {

    Optional<MachineEvent> findByEventId(String eventId);

    @Query("""
       SELECT COUNT(e) FROM MachineEvent e
       WHERE e.machineId = :machineId AND e.eventTime >= :start AND e.eventTime < :end
    """)
    long countEvents(@Param("machineId") String machineId,
                     @Param("start") Instant start,
                     @Param("end") Instant end);

    @Query("""
       SELECT COALESCE(SUM(e.defectCount),0) FROM MachineEvent e
       WHERE e.machineId = :machineId 
       AND e.defectCount >= 0
       AND e.eventTime >= :start AND e.eventTime < :end
    """)
    long sumDefects(@Param("machineId") String machineId,
                    @Param("start") Instant start,
                    @Param("end") Instant end);
}

