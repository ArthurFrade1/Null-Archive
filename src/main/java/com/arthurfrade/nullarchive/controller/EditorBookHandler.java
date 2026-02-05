package com.arthurfrade.nullarchive.controller;

import com.arthurfrade.nullarchive.dto.ApiError;
import com.arthurfrade.nullarchive.dto.ApiResponse;
import com.arthurfrade.nullarchive.dto.BookRequest;
import com.arthurfrade.nullarchive.dto.UserSessionData;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.arthurfrade.nullarchive.util.CorsUtil;
import com.arthurfrade.nullarchive.util.HttpUtil;
import com.arthurfrade.nullarchive.util.MultipartUtil;
import com.arthurfrade.nullarchive.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

public class EditorBookHandler implements HttpHandler {
    private final UserRepository repo;
    private static final ObjectMapper mapper = new ObjectMapper();

private static final Path UPLOAD_FILE = Paths.get(
    System.getProperty("user.home"),
    "Desktop",
    "nullarchive_uploads",
    "books"
);
private static final Path UPLOAD_IMAGE = Paths.get(
    System.getProperty("user.home"),
    "Desktop",
    "nullarchive_uploads",
    "images"
);


    public EditorBookHandler(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        CorsUtil.addCorsHeaders(exchange);
        if (CorsUtil.handlePreflight(exchange)) return;
        if (!HttpUtil.requireMethod(exchange, "POST")) return;

        String ct = exchange.getRequestHeaders().getFirst("Content-Type");
        if (ct == null || !ct.toLowerCase().startsWith("multipart/form-data")) {
            HttpUtil.sendJson(exchange, 415, new ApiError("Expected multipart/form-data"));
            return;
        }

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
            HttpUtil.sendText(exchange, 500, "Erro interno");
            return;
        }

        //Usuário nesse ponto é valido

        Map<String, MultipartUtil.Part> parts;
        try {
            parts = MultipartUtil.parse(exchange);
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 400, new ApiError("Invalid multipart"));
            return;
        }

        MultipartUtil.Part metaPart = parts.get("meta");
        MultipartUtil.Part filePart = parts.get("file");
        MultipartUtil.Part imagePart = parts.get("image");

        if (metaPart == null || filePart == null || imagePart == null) {
            HttpUtil.sendJson(exchange, 400, new ApiError("Missing meta, file or image"));
            return;
        }

        
        BookRequest meta;
        try {
            meta = mapper.readValue(metaPart.asStringUtf8(), BookRequest.class);
        } catch (Exception e) {
            HttpUtil.sendJson(exchange, 400, new ApiError("Invalid meta JSON"));
            return;
        }
        
        if(meta.tags.length > 3)
            HttpUtil.sendText(exchange, 400, "Mais de 3 tags selecionadas");

        
        try {
            Files.createDirectories(UPLOAD_FILE);
            Files.createDirectories(UPLOAD_IMAGE);
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Could not create upload directory"));
            return;
        }

        //File
        String ext = guessExtension(meta.file_kind, filePart.filename);
        
        String safeName = UUID.randomUUID().toString().replace("-", "") + ext;
        
        Path savedPathFile = UPLOAD_FILE.resolve(safeName).normalize();
        
        try {
            Files.write(savedPathFile, filePart.data);
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Could not save file"));
            return;
        }
        
        //Image
        safeName = UUID.randomUUID().toString().replace("-", "") + ".png";

        Path savedPathImage = UPLOAD_IMAGE.resolve(safeName).normalize();

        try {
            Files.write(savedPathImage, imagePart.data);
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Could not save image"));
            return;
        }

        // 1. file_kind (Vem do JSON do front-end)
        String fileKind = meta.file_kind; 

        // 2. storage_path (Caminho relativo para salvar no banco)
        // Dica: Recomendo salvar o caminho relativo ao UPLOAD_FILE para facilitar se mudar o servidor de lugar
        String storagePathFile =savedPathFile.getFileName().toString();
        String storagePathImage = savedPathImage.getFileName().toString();

        // 3. original_filename (Nome original que o usuário subiu)
        String originalFilename = filePart.filename;

        // 4. mime_type (Extraído do cabeçalho do multipart)
        String mimeType = filePart.contentType; 

        // 5. size_bytes (Tamanho real do array de bytes)
        long sizeBytes = (long) filePart.data.length;

        try {
            //Insere informações do livro no banco
            int idBook = repo.createBook(meta.title, meta.author_name, meta.description, meta.language_code, meta.published_year, meta.license, userData.id, meta.source_url, meta.era);
            //Cria ligações entre as tags e o livro
            repo.createBookTag(idBook, meta.tags);

            repo.createBookFiles(idBook, fileKind, storagePathFile, storagePathImage, originalFilename, mimeType, sizeBytes);
            
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtil.sendJson(exchange, 500, new ApiError("Could not save file"));
            return;
        }

        String msg = "Recebido: title=" + meta.title +
                     ", file=" + (filePart.filename == null ? "(no name)" : filePart.filename) +
                     ", bytes=" + filePart.data.length +
                     ", savedAs=" + savedPathFile.toString();



        HttpUtil.sendJson(exchange, 200, new ApiResponse(msg));
    }

    private static String guessExtension(String fileKind, String originalFilename) {
        if (originalFilename != null) {
            int dot = originalFilename.lastIndexOf('.');
            if (dot >= 0 && dot < originalFilename.length() - 1) {
                String ext = originalFilename.substring(dot).toLowerCase();
                if (ext.length() <= 10) return ext;
            }
        }

        if (fileKind == null) return ".bin";

        switch (fileKind) {
            case "PDF": return ".pdf";
            case "EPUB": return ".epub";
            case "MOBI": return ".mobi";
            case "TXT": return ".txt";
            default: return ".bin";
        }
    }
    
}
