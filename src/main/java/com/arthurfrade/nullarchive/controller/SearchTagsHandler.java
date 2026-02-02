package com.arthurfrade.nullarchive.controller;


import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SearchTagsHandler implements HttpHandler {

    private final UserRepository repo;

    public SearchTagsHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "GET")) return;


        Map<String, String> params = HttpUtil.parseQueryParams(exchange); // Se tiver essa utilitária
        String tagsParam = params.get("tags"); 
        String[] tagsArray = (tagsParam != null) ? tagsParam.split(",") : new String[0];

        List<Map<String, Object>> books ;
        if (params.containsKey("admin")) {
            books = repo.getBooksByTags(tagsArray, 0);
        } else{
            books = repo.getBooksByTags(tagsArray, 1);
        }

        // 2. O HttpUtil.sendJson vai converter essa Lista para o formato [{}, {}]
        HttpUtil.sendJson(exchange, 200, books);

    }

}
