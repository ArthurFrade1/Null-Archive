package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.ApiError;
import com.arthurfrade.nullarchive.dto.ApiResponse;
import com.arthurfrade.nullarchive.dto.LoginRequest;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.Validation;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class LoginHandler implements HttpHandler {
    private final UserRepository repo;

    public LoginHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;

        if (!HttpUtil.requireMethod(exchange, "POST")) return;
        if (!HttpUtil.requireJson(exchange)) return;

        LoginRequest req;
        try {
            req = HttpUtil.readJson(exchange, LoginRequest.class);
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 400, new ApiError("Invalid JSON"));
            return;
        }

        String err = Validation.validateLogin(req);
        if (err != null) {
            HttpUtil.sendJson(exchange, 400, new ApiError(err));
            return;
        }

        String storedHash;
        try {
            storedHash = repo.findPasswordHashByEmail(req.email);
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Erro interno"));
            return;
        }

        if (storedHash == null || !BCrypt.checkpw(req.password, storedHash)) {
            HttpUtil.sendJson(exchange, 401, new ApiError("Credenciais inválidas"));
            return;
        }

        HttpUtil.sendJson(exchange, 200, new ApiResponse("Login OK"));
    }
}
