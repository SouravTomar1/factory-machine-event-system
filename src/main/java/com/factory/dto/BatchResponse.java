package com.factory.dto;

import java.util.*;

public class BatchResponse {
    public int accepted;
    public int deduped;
    public int updated;
    public int rejected;
    public List<Map<String, String>> rejections = new ArrayList<>();
}
