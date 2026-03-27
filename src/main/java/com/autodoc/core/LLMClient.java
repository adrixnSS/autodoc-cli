package com.autodoc.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LLMClient {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public LLMClient(boolean isLocal) {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();

        if (isLocal) {
            this.baseUrl = "http://127.0.0.1:18789/v1/chat/completions";
        } else {
            // Usamos endpoint proxy compatible con formato unificado (OpenClaw) que encamina a Gemini.
            this.baseUrl = "https://gateway.api/v1/chat/completions"; 
        }
    }

    public String generateDocumentation(String prunedCodeBlocks) throws Exception {
        ObjectNode requestBody = mapper.createObjectNode();
        
        String systemPrompt = "Eres un Technical Writer experto y un Arquitecto de Software. Tu tarea es extraer documentación detallada basada EXCLUSIVAMENTE en la estructura AST de Java provista. NO inventes implementaciones (alucinaciones). Documenta firmas, patrones arquitectónicos detectados y sugiere siempre al menos un diagrama conceptual usando formato Mermaid (\n```mermaid ... ```\n).";

        ArrayNode messages = requestBody.putArray("messages");

        ObjectNode systemMsg = mapper.createObjectNode();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", "Estructura del código (AST Podado):\n" + prunedCodeBlocks);
        messages.add(userMsg);

        requestBody.put("model", "gemini-1.5-pro");

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json");

        if (!baseUrl.contains("127.0.0.1")) {
             builder.header("Authorization", "Bearer " + System.getenv("GEMINI_API_KEY"));
        }

        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Parsear respuesta genérica
        JsonNode jsonNode = mapper.readTree(response.body());
        try {
            return jsonNode.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            return "Error parseando respuesta: " + response.body();
        }
    }
}
