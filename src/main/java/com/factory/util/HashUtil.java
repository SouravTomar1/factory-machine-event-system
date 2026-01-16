package com.factory.util;

import com.factory.dto.EventRequest;
import org.apache.commons.codec.digest.DigestUtils;

public class HashUtil {
    public static String hash(EventRequest e) {
        String raw = e.eventId + e.eventTime + e.machineId + e.durationMs + e.defectCount;
        return DigestUtils.sha256Hex(raw);
    }
}
