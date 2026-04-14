package com.ben.sdet.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;

public final class YamlLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    private YamlLoader() {
        // Utility class
    }

    public static <T> T read(String yaml, Class<T> valueType) throws IOException {
        return YAML_MAPPER.readValue(yaml, valueType);
    }
}
