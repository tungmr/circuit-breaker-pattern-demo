package com.tungmr;


import java.io.IOException;
import java.io.OutputStream;

public class ExternalService {

    public static void main(String[] args) throws IOException {
        SimpleHttpServer externalService = new SimpleHttpServer(8080);
        externalService.addGetMapping("/external-service", (exchange -> {
            if ("GET".equals(exchange.getRequestMethod())) {
                String response = "This is a response text from external service";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(response.getBytes());
                output.flush();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
            exchange.close();
        }));
        externalService.start();
    }
}