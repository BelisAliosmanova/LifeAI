package com.lifeAI.LifeAI.utils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class MapUtils {
    public static Map<String, Object> stringToMap(String data) {
        String trimmedData = data.substring(1, data.length() - 1);

        return Arrays.stream(trimmedData.split(", (?=\\S+=)"))
                .map(x -> x.split("=", 2))
                .collect(
                        Collectors.toMap(
                                x -> x[0].trim(),
                                x -> x[1].trim()
                        )
                );
    }
}