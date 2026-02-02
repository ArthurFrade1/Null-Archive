package com.arthurfrade.nullarchive.controller;


import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class BookInfoHandler implements HttpHandler {

    private final UserRepository repo;

    public BookInfoHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "GET")) return;

        String query = exchange.getRequestURI().getQuery();
        int bookId = 0;
        String bookIdText = null;
    
        if (query != null && query.contains("=")) {
        // Divide no "=" e pega a segunda parte (o valor)
            bookIdText = query.split("=")[1];

        }
        // 3. Validação básica
        if (bookIdText == null) {
            HttpUtil.sendText(exchange, 400,"ID não fornecido");
            return;
        }
        bookId = Integer.parseInt(bookIdText);


        // 1. Chame a função que retorna a LISTA de tags
        Map<String, Object> book = repo.getBooksInfo(bookId);

        // 2. O HttpUtil.sendJson vai converter essa Lista para o formato [{}, {}]
        HttpUtil.sendJson(exchange, 200, book);

    }

}
