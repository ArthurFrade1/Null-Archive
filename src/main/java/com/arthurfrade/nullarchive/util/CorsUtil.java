package com.arthurfrade.nullarchive.util;

import com.sun.net.httpserver.HttpExchange;

public class CorsUtil {
    private static final String DEV_ORIGIN = "http://127.0.0.1:5500";

    public static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", DEV_ORIGIN);
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    public static boolean handlePreflight(HttpExchange exchange) throws java.io.IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }
}
