package com.arthurfrade.nullarchive.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class CorsUtil {

    public static void addCorsHeaders(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");

        if (origin != null && (isAllowedDevOrigin(origin))) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            exchange.getResponseHeaders().set("Vary", "Origin");
            exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        }

        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "600");
    }

    private static boolean isAllowedDevOrigin(String origin) {
        return origin.equals("http://192.168.1.2:5500")
            || origin.equals("http://localhost:5500")
            || origin.startsWith("http://192.168.")
            || origin.startsWith("http://10.")
            || origin.startsWith("http://172.16.")
            || origin.startsWith("http://172.17.")
            || origin.startsWith("http://172.18.")
            || origin.startsWith("http://172.19.")
            || origin.startsWith("http://172.2")
            || origin.startsWith("http://172.3");
    }

    public static boolean handlePreflight(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            addCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }
}
