package com.arthurfrade.nullarchive.controller;

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

        // 1. Chame a função que retorna a LISTA de tags
        List<Map<String, Object>> tags = repo.getTags();

        // 2. O HttpUtil.sendJson vai converter essa Lista para o formato [{}, {}]
        HttpUtil.sendJson(exchange, 200, tags);

    }
}
