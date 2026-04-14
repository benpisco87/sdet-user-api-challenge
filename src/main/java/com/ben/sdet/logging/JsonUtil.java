package com.ben.sdet.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonUtil() {
        // Utility class
    }

    public static String pretty(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        try {
            Object obj = mapper.readValue(json, Object.class);
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return json;
        }
    }
}
