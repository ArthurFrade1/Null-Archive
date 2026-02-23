package com.arthurfrade.nullarchive;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class StaticFileHandler implements HttpHandler {
    private final String basePath;

    public StaticFileHandler(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Se pedir a raiz "/", entregamos a home
        if (path.equals("/home")) 
            path = "/pages/home/home.html";
        else if(path.equals("/livro"))
            path = "/pages/livro/livro.html";   
        else if(path.equals("/login"))
            path = "/pages/login/login.html";   
        else if(path.equals("/cadastro"))
            path = "/pages/cadastro/cadastro.html";   
        else if(path.equals("/publicar"))
            path = "/pages/publicar/publicar.html";   
        else if(path.equals("/curadoria"))
            path = "/pages/curadoria/curadoria.html";   
        

        File file = new File(basePath + path);

        if (file.exists() && !file.isDirectory()) {
            // Identifica se é CSS, JS, HTML ou Imagem
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) contentType = "application/octet-stream";

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, file.length());

            try (OutputStream os = exchange.getResponseBody(); 
                 FileInputStream fis = new FileInputStream(file)) {
                fis.transferTo(os);
            }
        } else {
            // Aqui entra a sua lógica de segurança: Se não achou, manda 404
            String response = "404 (Not Found)";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}