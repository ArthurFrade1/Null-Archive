package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.ApiError;
import com.arthurfrade.nullarchive.dto.ApiResponse;
import com.arthurfrade.nullarchive.dto.AuthenticatedUserRequest;
import com.arthurfrade.nullarchive.dto.LoginRequest;
import com.arthurfrade.nullarchive.dto.UserSessionData;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.TokenUtil;
import com.arthurfrade.nullarchive.util.Validation;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.Map;


public class EditorLogoutHandler implements HttpHandler {

    private final UserRepository repo;

    public EditorLogoutHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "POST")) return;

        String token = TokenUtil.getCookieValue(exchange, "account_token");

        try {
            repo.logoutAccountSession(token);                            //Cria sessäo no banco
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Erro interno"));
            return;
        }


        exchange.getResponseHeaders().add(                                                //Coloca token no Set-Cookie
            "Set-Cookie",
            "account_token="+
            "; Path=/; HttpOnly; SameSite=Lax; Max-Age=0;"
        );

        HttpUtil.sendJson(                                                                 //Responde
            exchange,
            200,
            new ApiResponse("Logout")
        );
    }
}
