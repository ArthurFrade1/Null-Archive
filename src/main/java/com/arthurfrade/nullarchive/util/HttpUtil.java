package com.arthurfrade.nullarchive.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean requireMethod(HttpExchange exchange, String method) throws java.io.IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase(method)) {
            sendText(exchange, 405, "Use " + method);
            return false;
        }
        return true;
    }

    public static boolean requireJson(HttpExchange exchange) throws java.io.IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            sendText(exchange, 415, "Expected application/json");
            return false;
        }
        return true;
    }

    public static <T> T readJson(HttpExchange exchange, Class<T> clazz) throws java.io.IOException {
        try (InputStream is = exchange.getRequestBody()) {
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return mapper.readValue(body, clazz);
        }
    }

    public static void sendJson(HttpExchange exchange, int status, Object payload) throws java.io.IOException {
        byte[] bytes = mapper.writeValueAsBytes(payload);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendText(HttpExchange exchange, int status, String body) throws java.io.IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
