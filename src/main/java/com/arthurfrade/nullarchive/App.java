package com.arthurfrade.nullarchive;

import com.arthurfrade.nullarchive.controller.EditorLoginHandler;
import com.arthurfrade.nullarchive.controller.EditorRegisterHandler;
import com.arthurfrade.nullarchive.controller.EditorBookHandler;
import com.arthurfrade.nullarchive.controller.UserTokenHandler;
import com.arthurfrade.nullarchive.controller.EditorDataHandler;
import com.arthurfrade.nullarchive.controller.EditorLogoutHandler;
import com.arthurfrade.nullarchive.controller.EditorTagsHandler;
import com.arthurfrade.nullarchive.repository.UserRepository;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;

public class App {
    public static void main(String[] args) throws Exception {
        int port = 8081;

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        UserRepository repo = new UserRepository();

        server.createContext("/editor/register",   new EditorRegisterHandler(repo));  //POST /editor/register
        server.createContext("/editor/login",      new EditorLoginHandler(repo));     //POST /editor/login
        server.createContext("/editor/data",       new EditorDataHandler(repo));      //GET  /editor/data
        server.createContext("/user/token",        new UserTokenHandler(repo));       //POST /user/token
        server.createContext("/editor/book",       new EditorBookHandler(repo));       //POST /editor/book
        server.createContext("/editor/tags",       new EditorTagsHandler(repo));       //GET /editor/tags
        server.createContext("/editor/logout",     new EditorLogoutHandler(repo));       //GET /editor/logout

        server.setExecutor(null);
        server.start();

        System.out.println("Servidor rodando em http://localhost:" + port);
    }
}
