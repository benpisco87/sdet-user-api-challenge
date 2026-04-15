package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public final class YamlLoader {

    private static final ObjectMapper objectMapper =
            new ObjectMapper(new YAMLFactory());

    private YamlLoader() {}

    public static <T> List<T> load(String resourcePath, String key, Class<T> clazz) {
        InputStream input = YamlLoader.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (input == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        try {
            Map<String, Object> yaml = objectMapper.readValue(input, Map.class);
            Object section = yaml.get(key);

            return objectMapper.convertValue(
                    section,
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, clazz)
            );
        } catch (Exception e) {
            throw new RuntimeException("Error loading YAML: " + resourcePath, e);
        }
    }
}
