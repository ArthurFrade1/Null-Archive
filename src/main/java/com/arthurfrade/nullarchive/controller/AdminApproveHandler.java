package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.ApiError;
import com.arthurfrade.nullarchive.dto.ApproveBook;
import com.arthurfrade.nullarchive.dto.UserSessionData;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.TokenUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.IOException;

public class AdminApproveHandler implements HttpHandler {

    private final UserRepository repo;

    public AdminApproveHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "POST")) return;

        String token = TokenUtil.getCookieValue(exchange, "account_token");

        
        UserSessionData userData;
        try {
            userData = repo.getEditorData(token);
            if(userData == null){
                HttpUtil.sendText(exchange, 400, "Usuário não autenticado"); //Usuário 
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendText(exchange, 500, "Erro");
            return;
        }
        
        if(userData.role=="EDITOR"){
            HttpUtil.sendText(exchange, 400, "Usuário não é admininstrador"); //Usuário 
            return ;
        }

        //Usuário é ADMIN

        ApproveBook req;
        try {
            req = HttpUtil.readJson(exchange, ApproveBook.class);   //Lê dados JSON
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 400, new ApiError("Invalid JSON"));
            return;
        }
        if("approve".equals(req.action)){
            repo.approveBook(req.id);
            HttpUtil.sendText(exchange, 200, "Arquivo aprovado com sucesso");

        }if("desapprove".equals(req.action)){
            String[] paths = repo.getFileName(req.id);
            String dirFile = "C:/Users/arthu/Desktop/nullarchive_uploads/books/";
            String dirImage = "C:/Users/arthu/Desktop/nullarchive_uploads/images/";
            
            File file = new File(dirFile, paths[0]);
            System.out.println( file.getAbsolutePath() );
            
            if(file.exists()){
                if(!file.delete()){
                    HttpUtil.sendText(exchange, 400, "Falha ao deletar livro, pode estar em uso por outro processo");
                }
                
            }else{
                HttpUtil.sendText(exchange, 400, "Livro não existe no diretório");
            }

            
            File image = new File(dirImage, paths[1]);

            if(image.exists()){
                if(!image.delete()){
                    HttpUtil.sendText(exchange, 400, "Falha ao deletar imagem, pode estar em uso por outro processo");
                }
                
            }else{
                HttpUtil.sendText(exchange, 400, "Imagem não existe no diretório");
            }

            repo.deleteBookInfo(req.id);
            HttpUtil.sendJson(exchange, 200, "Livro deletado com sucesso");


        }
    }
}
