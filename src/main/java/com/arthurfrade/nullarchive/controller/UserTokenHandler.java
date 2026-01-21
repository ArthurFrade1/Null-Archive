package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.ApiError;
import com.arthurfrade.nullarchive.dto.ApiResponse;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.TokenUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


import java.io.IOException;

public class UserTokenHandler implements HttpHandler {

    //Tempo máximo definido, 400 dias
    private static final long SESSION_DAYS = 400;

    private static final long maxAge = SESSION_DAYS * 24 * 60 * 60;

    private final UserRepository repo;

    public UserTokenHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "POST")) return;


        // Vê se o token existe

        String token = getTokenValue(exchange, "user_token");

        try{
            if (repo.userExists(token)) {

                //Se existir, renovo sua existência com mais 400 dias
                //Se o usuário não entrar no site por 400 dias perde seus dados
        
                exchange.getResponseHeaders().add(
                    "Set-Cookie",
                    "user_token=" + token +
                    "; Path=/; HttpOnly; SameSite=Lax; Max-Age=" + maxAge
                );
        
                // 7️⃣ Resposta final
                HttpUtil.sendText(
                    exchange,
                    200,
                    "Token existe"
                );
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Erro interno"));
            return;
        }


        //Token nao existe 

        // 4️⃣ Gera token de sessão
        token = TokenUtil.generateSessionTokenHex64();

        // 5️⃣ Salva sessão no banco
        try {
            repo.createUserSession(token);
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Erro interno"));
            return;
        }

        exchange.getResponseHeaders().add(
            "Set-Cookie",
            "user_token=" + token +
            "; Path=/; HttpOnly; SameSite=Lax; Max-Age=" + maxAge
        );

        // 7️⃣ Resposta final
        HttpUtil.sendJson(
            exchange,
            200,
            new ApiResponse("Token criado")
        );
    }

    private static String getTokenValue(HttpExchange exchange, String name) {
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
