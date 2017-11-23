package com.example.spider.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;

/**
 * @author LiuQi - [Created on 2017-11-23]
 */
public class JsonUtil {

    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return StringUtils.EMPTY;
        }
    }

    public static <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String readStringField(String json, String key) {
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            return jsonNode.get(key).asText();
        } catch (IOException e) {
            e.printStackTrace();
            return StringUtils.EMPTY;
        }
    }
}
