package com.tungmr;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;

public class InternalService {


    public static void main(String[] args) throws IOException {

        SimpleHttpServer internalService = new SimpleHttpServer(8181);
        internalService.addGetMapping("/test", (exchange -> {

            if ("GET".equals(exchange.getRequestMethod())) {
                StringBuilder result = new StringBuilder("Response from external service:");
                try {
                    result.append(RequestUtils.get("http://127.0.0.1:8080/external-service"));
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                exchange.sendResponseHeaders(200, result.toString().getBytes().length);
                OutputStream output = exchange.getResponseBody();
                output.write(result.toString().getBytes());
                output.flush();


            } else {
                exchange.sendResponseHeaders(405, -1);
            }
            exchange.close();
        }));
        internalService.start();
    }
}