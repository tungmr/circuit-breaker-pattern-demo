package com.tungmr;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class SimpleHttpServer {
    private final int port;
    private final HttpServer server;
    public SimpleHttpServer(int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(this.port), 0);
    }

    public void addGetMapping(String path, HttpHandler handler) {
        server.createContext(path, handler);
    }

    public void start() {
        this.server.start();
        System.out.println("Server started on port " + this.port);
    }
}
