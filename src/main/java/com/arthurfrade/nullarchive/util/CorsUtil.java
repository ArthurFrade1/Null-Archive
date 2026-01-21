package com.arthurfrade.nullarchive.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Set;

public class CorsUtil {

    // Coloque aqui os origins permitidos (DEV)
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
        "http://127.0.0.1:5500",
        "http://localhost:5500"
    );

    public static void addCorsHeaders(HttpExchange exchange) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");

        // Só devolve CORS se o Origin for permitido
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            exchange.getResponseHeaders().set("Vary", "Origin"); // importante quando há múltiplos origins
            exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        }

        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "600");
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
