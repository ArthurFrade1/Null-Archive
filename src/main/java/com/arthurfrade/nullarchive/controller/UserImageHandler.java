package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class UserImageHandler implements HttpHandler {
    private final String baseDir = "C:\\Users\\arthu\\Desktop\\nullarchive_uploads\\images\\"; 

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // 1. Adiciona CORS (se não o navegador bloqueia a imagem)
        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "GET")) return;


        // 2. Extrai o nome do arquivo da URL (ex: /uploads/capa1.jpg -> capa1.jpg)
            // A URL será algo como: /user/image/nome-da-imagem.png
        String path = exchange.getRequestURI().getPath();
        
        // Pega tudo que vem depois da última "/"
        String fileName = path.substring(path.lastIndexOf("/") + 1);
            
        File file = new File(baseDir, fileName);

        // 3. SEGURANÇA: Verifica se o caminho absoluto do arquivo 
        // ainda começa com o caminho do seu diretório base.
        String canonicalPath = file.getCanonicalPath();
        if (!canonicalPath.startsWith(new File(baseDir).getCanonicalPath())) {
            // Se o caminho "resolvido" for fora da pasta images, bloqueia!
            String response = "Acesso Negado: Tentativa de Path Traversal";
            exchange.sendResponseHeaders(403, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            return;
}

        if (!file.exists() || file.isDirectory()) {
            String response = "Arquivo não encontrado";
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            return;
        }

        // 3. Detecta o tipo do arquivo (image/jpeg, application/pdf, etc)
        String mimeType = Files.probeContentType(file.toPath());
        exchange.getResponseHeaders().set("Content-Type", mimeType != null ? mimeType : "application/octet-stream");

        // 4. Envia o arquivo
        exchange.sendResponseHeaders(200, file.length());
        try (FileInputStream fis = new FileInputStream(file); 
             OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
        }
        exchange.close();
    }
}