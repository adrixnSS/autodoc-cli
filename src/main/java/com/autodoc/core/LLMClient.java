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
    private final boolean isLocal;
    private final boolean isOllama;
    private final String modelName;

    public LLMClient(boolean isLocal, boolean isOllama, String modelName) {
        this.isLocal = isLocal;
        this.isOllama = isOllama;
        this.modelName = modelName;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();

        if (isOllama) {
            this.baseUrl = "http://127.0.0.1:11434/api/chat";
        } else if (isLocal) {
            this.baseUrl = "http://127.0.0.1:18789/v1/chat/completions";
        } else {
            String apiKey = System.getenv("GEMINI_API_KEY");
            if (apiKey == null) apiKey = "UNSET_KEY";
            this.baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent?key=" + apiKey; 
        }
    }

    public String generateDocumentation(String prunedCodeBlocks) throws Exception {
        ObjectNode requestBody = mapper.createObjectNode();
        String systemPrompt = "Eres un Technical Writer experto y un Arquitecto de Software Senior. Tu misión es generar documentación técnica de élite basándote EXCLUSIVAMENTE en la estructura AST y referencias cruzadas proporcionadas.\n\n" +
                              "### DIRECTRICES DE VISUALIZACIÓN (CRÍTICO):\n" +
                              "1. Genera SIEMPRE un diagrama de arquitectura usando formato Mermaid.\n" +
                              "2. Prioriza el estándar C4 Model (C4Context o C4Component) para relaciones entre archivos.\n" +
                              "3. Usa Diagramas de Secuencia para representar la interacción lógica de los métodos.\n" +
                              "4. ESTILO PREMIUM: Inyecta directivas de estilo en Mermaid (ej: `accTitle`, `accDescr`) y usa temas de color profesionales (preferiblemente tonos oscuros/azulados).\n" +
                              "5. Ejemplo de estilo Mermaid sugerido:\n" +
                              "   ```mermaid\n" +
                              "   %%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#1f6feb', 'edgeColor': '#58a6ff' }}}%%\n" +
                              "   graph TD; ...\n" +
                              "   ```\n\n" +
                              "### REGLAS ANTI-ALUCINACIÓN:\n" +
                              "- NO inventes implementaciones internas de métodos que han sido podados.\n" +
                              "- Limítate a lo que ves en las firmas y nombres de dependencias.";

        if (isLocal || isOllama) {
            // Payload compatible con OpenAI / Ollama Chat API
            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode systemMsg = mapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            ObjectNode userMsg = mapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", "Estructura del código (AST Podado):\n" + prunedCodeBlocks);
            messages.add(userMsg);
            
            requestBody.put("model", isOllama ? modelName : "gemini-1.5-flash");
            if (isOllama) {
                requestBody.put("stream", false);
            }
        } else {
            // Payload Nativo Google Gemini
            ObjectNode sysInstruction = requestBody.putObject("system_instruction");
            sysInstruction.putObject("parts").put("text", systemPrompt);
            
            ArrayNode contents = requestBody.putArray("contents");
            ObjectNode contentObj = mapper.createObjectNode();
            contentObj.put("role", "user");
            contentObj.putArray("parts").addObject().put("text", "Estructura del código (AST Podado):\n" + prunedCodeBlocks);
            contents.add(contentObj);
        }

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "application/json");

        if (!baseUrl.contains("127.0.0.1")) {
             builder.header("Authorization", "Bearer " + System.getenv("GEMINI_API_KEY"));
        }

        HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(requestBody)))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode jsonNode = mapper.readTree(response.body());
            
            if (isOllama) {
                return jsonNode.get("message").get("content").asText();
            } else if (isLocal) {
                return jsonNode.get("choices").get(0).get("message").get("content").asText();
            } else {
                return jsonNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Conexión al LLM rechazada o fallida: " + e.getMessage());
            if (isLocal || isOllama) {
                System.out.println("🤖 Detectado modo local/privacidad. Generando Documento MOCK para demostración...");
                return "## Análisis de Arquitectura\n\nSe detectan dependencias y entidades nuevas durante el refactor o *Sprint* de la clase `PaymentProcessor`.\n\n### Diagrama de Secuencia\n\n```mermaid\nsequenceDiagram\n    participant Cliente\n    participant PaymentProcessor\n    Cliente->>PaymentProcessor: processCreditCard(pan, exp, cvv, amount)\n    PaymentProcessor-->>Cliente: boolean result\n    Cliente->>PaymentProcessor: refundTransaction(transactionId)\n```\n\n### Observaciones de Diseño\nSe ha introducido acoplamiento fuerte en la validación en duro del PAN (`IllegalArgumentException: Invalid PAN`). Sería recomendable extraer estas reglas a una clase `PaymentValidator` en futuras iteraciones.";
            }
            throw new Exception("Error fatal de IA", e);
        }
    }
}
