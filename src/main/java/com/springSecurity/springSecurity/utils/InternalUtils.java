package com.springSecurity.springSecurity.utils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

public class InternalUtils {
    public static byte[] b64DecodeToBytes(String str) {
        byte[] result = null;
        StringBuilder buf = new StringBuilder(str);
        while (result == null && !buf.isEmpty()) {
            try {
                result = Base64.getUrlDecoder().decode(buf.toString());
            } catch (IllegalArgumentException e) {
                buf.deleteCharAt(buf.length() - 1);
            }
        }
        return result == null ? new byte[0] : result;
    }

    public static String b64Decode(String str) {
        return new String(b64DecodeToBytes(str));
    }

    public static String b64Encode(String str) {
        return Base64.getUrlEncoder().encodeToString(str.getBytes());
    }
    public static <T> Optional<List<T>> toList(String str, int minSize, Function<String, T> parser, String sep) {
        if (str == null || str.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Arrays.stream(str.split(sep)).map(parser).toList())
                .filter(values -> values.size() >= minSize);
    }

    public static <T> T parseIndex(String str, T[] array) {
        final var value = Integer.parseInt(str);
        return array[value >= array.length ? 0 : value];
    }

    public static String urlDecode(String str) {
        if (str.isEmpty()) {
            return null;
        }

        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

    public static Map<Integer, String> splitToMap(String str, String regex) {
        Map<Integer, String> map = new HashMap<>();
        String[] splitted = str.split(regex);

        for (int i = 0; i < splitted.length; i += 2)
            map.put(Integer.parseInt(splitted[i]), i < splitted.length - 1 ? splitted[i + 1] : "");

        return map.isEmpty() ? Collections.emptyMap() : map;
    }

    public static void requireKeys(Map<Integer, String> data, int... keys) {
        for (var key : keys) {
            if (!data.containsKey(key)) {
                throw new IllegalStateException("Missing required key: " + key);
            }
        }
    }
}
