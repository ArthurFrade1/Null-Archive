package com.arthurfrade.nullarchive.controller;


import com.arthurfrade.nullarchive.dto.ApiError;
import com.arthurfrade.nullarchive.dto.ApiResponse;
import com.arthurfrade.nullarchive.dto.FavoriteRequest;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.TokenUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class UserFavoriteHandler implements HttpHandler {
    private final UserRepository repo;

    public UserFavoriteHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "POST")) return;
        if (!HttpUtil.requireJson(exchange)) return;

        FavoriteRequest req;
        try {
            req = HttpUtil.readJson(exchange, FavoriteRequest.class);   //Lê dados JSON
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 400, new ApiError("Invalid JSON"));
            return;
        }
        String user_token = TokenUtil.getCookieValue(exchange, "user_token");

        int user_id = repo.getUserId(user_token);

        if(req.is_fav)
            repo.createFavorite(req.book_id, user_id);
        else    
            repo.deleteFavorite(req.book_id, user_id);

        HttpUtil.sendJson(exchange, 200, new ApiResponse("Favorito criado"));
    }
}
