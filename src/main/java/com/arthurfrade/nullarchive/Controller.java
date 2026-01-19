
package com.arthurfrade.nullarchive;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mindrot.jbcrypt.BCrypt;

public class Controller {

    public static void main(String[] args) throws Exception {

        int port = 8081;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/users", new Users());
        server.createContext("/auth/login", new Login());


        server.setExecutor(null);
        server.start();

        System.out.println("Servidor rodando em http://localhost:" + port);
    }


    static class Users implements HttpHandler {

        private static final ObjectMapper mapper = new ObjectMapper();
        private static final UserRepository repo = new UserRepository();

            private static void addCorsHeaders(HttpExchange exchange) {
                // Para DEV: libera o Live Server
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
                // Se você for usar cookies/sessão depois, aí entra isso:
                // exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
            }


            @Override
            public void handle(HttpExchange exchange) throws IOException {

                addCorsHeaders(exchange);

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            // 1️⃣ Verifica método PERGUNTAR SE ISSO EH UTIL
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Use POST");
                return;
            }

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

            if (contentType == null || !contentType.contains("application/json")) {
                    sendResponse(exchange, 415, "Expected application/json");
                    return;
                }

            // 2️⃣ Lê o body
            String body = readRequestBody(exchange);

            User req;

            try {
                req = mapper.readValue(body, User.class);
            } catch (Exception e) {
                sendResponse(exchange, 400, "Invalid JSON");
                return;
            }

            String err = validate(req);
            if (err != null) {
                sendResponse(exchange, 400, err);
                return;
            }
            
            String passwordHash = BCrypt.hashpw(req.password, BCrypt.gensalt());
            // 3️⃣ salva no banco
            try {
                repo.createUser(req.username, passwordHash, req.role, req.email);
            } catch (SQLIntegrityConstraintViolationException e) {
                sendResponse(exchange, 409, "Usuário ou email já existe");
                return;
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Erro interno");
                return;
            }

            // 4️⃣ Responde
            sendResponse(exchange, 200, "User created");
        }
    }

    static class Login implements HttpHandler {

        private static final ObjectMapper mapper = new ObjectMapper();
        private static final UserRepository repo = new UserRepository();

            private static void addCorsHeaders(HttpExchange exchange) {
                // Para DEV: libera o Live Server
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
                exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
                exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
                // Se você for usar cookies/sessão depois, aí entra isso:
                // exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
            }


            @Override
            public void handle(HttpExchange exchange) throws IOException {

                addCorsHeaders(exchange);

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            // 1️⃣ Verifica método PERGUNTAR SE ISSO EH UTIL
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Use POST");
                return;
            }

            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");

            if (contentType == null || !contentType.contains("application/json")) {
                    sendResponse(exchange, 415, "Expected application/json");
                    return;
                }

            // 2️⃣ Lê o body
            String body = readRequestBody(exchange);

            User req;

            try {
                req = mapper.readValue(body, User.class);
            } catch (Exception e) {
                sendResponse(exchange, 400, "Invalid JSON");
                return;
            }

            String storedHash;

            try {
                storedHash = repo.findPasswordHashByEmail(req.email);
            } catch (SQLException e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Erro interno");
                return;
            }

            if (storedHash == null) {
                sendResponse(exchange, 401, "Credenciais inválidas");
                return;
            }

            boolean ok = BCrypt.checkpw(req.password, storedHash);

            if (ok) {
                sendResponse(exchange, 200, "Login OK");
            } else {
                sendResponse(exchange, 401, "Credenciais inválidas");
            }
        }
    }


    // ================= UTIL =================

    private static String validate(User req) {

        if (req.username == null || req.username.isBlank()) {
            return "Username obrigatório";
        }

        if (req.email == null || req.email.isBlank()) {
            return "Email obrigatório";
        }

        if (!req.email.contains("@")) {
            return "Email inválido";
        }

        if (req.password == null || req.password.length() < 6) {
            return "Senha deve ter ao menos 6 caracteres";
        }

        return null; // null = válido
    }


    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }


    private static void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}

