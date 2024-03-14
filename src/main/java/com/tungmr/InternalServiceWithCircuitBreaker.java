package com.tungmr;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class InternalServiceWithCircuitBreaker {

    private static final CircuitBreakerConfig config = CircuitBreakerConfig
            .custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(6)
            .minimumNumberOfCalls(3)
            .failureRateThreshold(50)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .slowCallRateThreshold(50)
            .permittedNumberOfCallsInHalfOpenState(4)
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .waitDurationInOpenState(Duration.ofSeconds(5))
            .build();

    private static final CircuitBreakerRegistry circuitBreakerRegistry =
            CircuitBreakerRegistry.of(config);

    private static final CircuitBreaker circuitBreaker = circuitBreakerRegistry
            .circuitBreaker("service");


    public static void main(String[] args) throws IOException {


        SimpleHttpServer internalService = new SimpleHttpServer(8181);
        internalService.addGetMapping("/test", (exchange -> {

            if ("GET".equals(exchange.getRequestMethod())) {

                CheckedSupplier<String> decoratedSupplier = CircuitBreaker
                        .decorateCheckedSupplier(circuitBreaker, () -> {
                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create("http://127.0.0.1:8080/external-service"))
                                    .method("GET", HttpRequest.BodyPublishers.noBody())
                                    .build();

                            HttpClient hc = HttpClient.newBuilder().build();
                            HttpResponse<String> httpResponse = hc.send(request, HttpResponse.BodyHandlers.ofString());
                            return httpResponse != null ? httpResponse.body() : "";
                        });

                try {
                    String s = decoratedSupplier.get();
                    exchange.sendResponseHeaders(200, s.getBytes().length);
                    OutputStream output = exchange.getResponseBody();
                    output.write(s.getBytes());
                    output.flush();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
            exchange.close();
        }));
        circuitBreaker.getEventPublisher().onEvent(System.out::println);
        internalService.start();
    }
}