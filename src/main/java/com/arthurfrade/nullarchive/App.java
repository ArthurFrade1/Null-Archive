package com.arthurfrade.nullarchive;

import com.arthurfrade.nullarchive.controller.EditorLoginHandler;
import com.arthurfrade.nullarchive.controller.EditorRegisterHandler;
import com.arthurfrade.nullarchive.controller.UserTokenHandler;
import com.arthurfrade.nullarchive.controller.EditorDataHandler;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws Exception {
        int port = 8081;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        UserRepository repo = new UserRepository();

        server.createContext("/editor/register",  new EditorRegisterHandler(repo));  //POST /auth/register
        server.createContext("/editor/login",     new EditorLoginHandler(repo));     //POST /auth/login
        server.createContext("/editor/data",      new EditorDataHandler(repo));            //GET /me
        server.createContext("/user/token",       new UserTokenHandler(repo));       //POST /visitor/init

        server.setExecutor(null);
        server.start();

        System.out.println("Servidor rodando em http://localhost:" + port);
    }
}
