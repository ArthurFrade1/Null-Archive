package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.ReadBookRequest;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.TokenUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public class UserReadingHandler implements HttpHandler {

    private final UserRepository repo;

    public UserReadingHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException{

        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "POST")) return;

        
        // Vê se o token existe
        
        String user_token = TokenUtil.getCookieValue(exchange, "user_token");
        
        try{
            if (!repo.userExists(user_token)) {
                
                HttpUtil.sendText( exchange, 400, "Token de usuário não registrado");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendText(exchange, 500, "Erro interno");
            return;
        }
        
        //Usuário existe
        
        ReadBookRequest req;
        
        req = HttpUtil.readJson(exchange, ReadBookRequest.class);   //Lê book id do JSON 

        int user_id = 0;
        try{
            user_id = repo.getUserId(user_token); // Lê user id do repositório
        }catch(Exception e){
            System.out.println("Ouve um problema");
        }

        repo.updateReadBook(req.book_id, user_id); // Atualiza o campo updated_at. Se não existir registro, cria.
        
        HttpUtil.sendText( exchange, 200, "Progresso atualizado");
    
    }
}
