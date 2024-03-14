package com.tungmr;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RequestUtils {


    public static String get(String uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response;
        HttpClient hc = HttpClient.newBuilder().build();
        response = hc.send(request, HttpResponse.BodyHandlers.ofString());
        return response != null ? response.body() : "";
    }

}
