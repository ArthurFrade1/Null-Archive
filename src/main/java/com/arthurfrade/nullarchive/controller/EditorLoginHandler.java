package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.ApiError;
import com.arthurfrade.nullarchive.dto.ApiResponse;
import com.arthurfrade.nullarchive.dto.AuthenticatedUserRequest;
import com.arthurfrade.nullarchive.dto.LoginRequest;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.TokenUtil;
import com.arthurfrade.nullarchive.util.Validation;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;


public class EditorLoginHandler implements HttpHandler {

    private final UserRepository repo;

    public EditorLoginHandler(UserRepository repo) {
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
            req = HttpUtil.readJson(exchange, LoginRequest.class);                //Lê JSON
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 400, new ApiError("Invalid JSON"));
            return;
        }

        String err = Validation.validateLogin(req);                                      //Valida
        if (err != null) {
            HttpUtil.sendJson(exchange, 400, new ApiError(err));
            return;
        }

        AuthenticatedUserRequest auth; 
        try {
            auth = repo.getEditorCredentials(req.email);                                   //Obtêm id e senha
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendText(exchange, 500, "Erro interno");
            return;
        }

        if (auth == null || !BCrypt.checkpw(req.password, auth.passwordHash)) {            //Compara senha
            HttpUtil.sendJson(exchange, 401, new ApiError("Credenciais inválidas")); 
            return;
        }
        
        //USUÁRIO VÁLIDO 
        String token = TokenUtil.generateSessionTokenHex64();                             //Gera token


        String ip = exchange.getRemoteAddress()                                           //Obtem IP
                            .getAddress()
                            .getHostAddress();

        String userAgent = exchange.getRequestHeaders()                                   //Obtem dados navegador
                                   .getFirst("User-Agent");

        try {
            repo.createEditorSession(auth.id, token, ip, userAgent);                            //Cria sessäo no banco
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendText(exchange, 500, "Erro interno");
            return;
        }


        exchange.getResponseHeaders().add(                                                //Coloca token no Set-Cookie
            "Set-Cookie",
            "account_token=" + token +
            "; Path=/; HttpOnly; SameSite=Lax;"
        );

        HttpUtil.sendJson(                                                                 //Responde
            exchange,
            200,
            new ApiResponse("Login OK")
        );
    }
}
