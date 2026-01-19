package com.arthurfrade.nullarchive;

import com.arthurfrade.nullarchive.controller.LoginHandler;
import com.arthurfrade.nullarchive.controller.UsersHandler;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws Exception {
        int port = 8081;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        UserRepository repo = new UserRepository();

        server.createContext("/users", new UsersHandler(repo));
        server.createContext("/auth/login", new LoginHandler(repo));

        server.setExecutor(null);
        server.start();

        System.out.println("Servidor rodando em http://localhost:" + port);
    }
}
