package com.chatp2p.managers;

import com.chatp2p.exceptions.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpManager {

    public static HttpResponse<String> getWithToken(String url, String token) {
        try {
            return HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Authorization", "Bearer " + token)
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (Exception e) {
            throw new NetworkException("Failed to execute GET with token", e);
        }
    }

    public static HttpResponse<String> postWithToken(String url, String token, String body) {
        try {
            return HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Authorization", "Bearer " + token)
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (Exception e) {
            throw new NetworkException("Failed to execute POST with token", e);
        }
    }

    public static HttpResponse<String> putWithToken(String url, String token, String body) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .PUT(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new NetworkException("Failed to execute PUT with token", e);
        }
    }
}