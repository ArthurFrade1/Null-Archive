package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.ApiError;
import com.arthurfrade.nullarchive.dto.ApiResponse;
import com.arthurfrade.nullarchive.dto.RegisterRequest;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.Validation;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

public class UsersHandler implements HttpHandler {
    private final UserRepository repo;

    public UsersHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;

        if (!HttpUtil.requireMethod(exchange, "POST")) return;
        if (!HttpUtil.requireJson(exchange)) return;

        RegisterRequest req;
        try {
            req = HttpUtil.readJson(exchange, RegisterRequest.class);
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 400, new ApiError("Invalid JSON"));
            return;
        }

        String err = Validation.validateRegister(req);
        if (err != null) {
            HttpUtil.sendJson(exchange, 400, new ApiError(err));
            return;
        }

        String passwordHash = BCrypt.hashpw(req.password, BCrypt.gensalt());

        try {
            repo.createUser(req.username, passwordHash, req.role, req.email);
        } catch (SQLIntegrityConstraintViolationException e) {
            HttpUtil.sendJson(exchange, 409, new ApiError("Usuário ou email já existe"));
            return;
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Erro interno"));
            return;
        }

        HttpUtil.sendJson(exchange, 201, new ApiResponse("User created"));
    }
}
