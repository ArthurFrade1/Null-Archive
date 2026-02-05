package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.UserSessionData;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.TokenUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SearchBooksHandler implements HttpHandler {

    private final UserRepository repo;

    public SearchBooksHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "GET")) return;


        Map<String, String> params = HttpUtil.parseQueryParams(exchange); // Se tiver essa utilitária
        String tagsParam = params.get("tags"); 
        String searchTerm = params.get("q"); 
        String[] tagsArray = (tagsParam != null) ? tagsParam.split(",") : new String[0];

        List<Map<String, Object>> books ;
        
        if (params.containsKey("admin")) {
            String account_token = TokenUtil.getCookieValue(exchange, "account_token");
            
            //Verifica se o usuário eh admininstrador
            
            
            UserSessionData userData;
            try {
                userData = repo.getEditorData(account_token);
                if(userData == null){
                    HttpUtil.sendText(exchange, 400, "Usuário não autenticado"); //Usuário 
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                HttpUtil.sendText(exchange, 500, "Erro");
                return;
            }
            
            if("EDITOR".equals(userData.role)){
                HttpUtil.sendText(exchange, 400, "Usuário não é admininstrador"); //Usuário 
                return ;
            }
            
            books = repo.searchCatalog(tagsArray, searchTerm, 0);

        }else if(params.containsKey("openbooks")){

            String user_token = TokenUtil.getCookieValue(exchange, "user_token");

            int user_id=repo.getUserId(user_token);
            books = repo.searchOpenBooks(user_id, tagsArray, searchTerm);
            books = repo.verificaFavorito(user_id, books);

        }else if(params.containsKey("favorites")){

            String user_token = TokenUtil.getCookieValue(exchange, "user_token");

            int user_id=repo.getUserId(user_token);
            books = repo.searchFavorites(user_id, tagsArray, searchTerm);
            books = repo.verificaOpenBooks(user_id, books);

        }else{ // Catalog
            String user_token = TokenUtil.getCookieValue(exchange, "user_token");

            books = repo.searchCatalog(tagsArray, searchTerm, 1);
            //Verifica se algum livro já foi aberto
            int user_id=repo.getUserId(user_token);
            books = repo.verificaOpenBooks(user_id, books);
            books = repo.verificaFavorito(user_id, books);
        }
        
        // 2. O HttpUtil.sendJson vai converter essa Lista para o formato [{}, {}]
        HttpUtil.sendJson(exchange, 200, books);
        
    }
    
}
