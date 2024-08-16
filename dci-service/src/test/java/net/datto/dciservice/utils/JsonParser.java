package net.datto.dciservice.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonParser {

    @SneakyThrows
    public static String convertToString(Object input) {
        return new ObjectMapper().writeValueAsString(input);
    }
}
