package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.ApiError;
import com.arthurfrade.nullarchive.dto.UserSessionData;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EditorTagsHandler implements HttpHandler {

    private final UserRepository repo;

    public EditorTagsHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "GET")) return;

        String token = getCookieValue(exchange, "account_token");

        
        UserSessionData userData;
        try {
            userData = repo.getEditorData(token);
            if(userData == null){
                HttpUtil.sendJson(exchange, 200, Map.of("authenticated", false)); //Usuário 
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Erro interno"));
            return;
        }

        // 1. Chame a função que retorna a LISTA de tags
        List<Map<String, Object>> tags = repo.getTags();

        // 2. O HttpUtil.sendJson vai converter essa Lista para o formato [{}, {}]
        HttpUtil.sendJson(exchange, 200, tags);

    }

    private static String getCookieValue(HttpExchange exchange, String name) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) return null;

        String[] parts = cookieHeader.split(";");
        for (String part : parts) {
            String[] kv = part.trim().split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return kv[1];
            }
        }
        return null;
    }
}
