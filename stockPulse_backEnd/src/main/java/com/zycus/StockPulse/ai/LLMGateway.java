package com.zycus.StockPulse.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.net.http.HttpClient;
import java.time.Duration;

import java.util.List;
import java.util.Map;

/** OpenAI-compatible gateway (LiteLLM, Groq, Ollama, etc.). */
@Component
public class LLMGateway {
    private final RestClient http;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;
    private final String productHeader;

    public LLMGateway(
            RestClient.Builder builder,
            ObjectMapper mapper,
            @Value("${llm.base-url:https://litellm-qc.zycus.net/v1}") String baseUrl,
            @Value("${llm.api-key:}") String apiKey,
            @Value("${llm.model:qwen-cursor}") String model,
            @Value("${llm.product-header:}") String productHeader) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(client);
        requestFactory.setReadTimeout(Duration.ofSeconds(20));
        this.http = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.model = model;
        this.productHeader = productHeader;
    }

    public String callLLM(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "Return only valid JSON matching the requested schema. Do not use markdown fences."),
                        Map.of("role", "user", "content", prompt)),
                "temperature", 0.2
        );

        var request = http.post().uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
        if (apiKey != null && !apiKey.isBlank()) {
            request.header("Authorization", "Bearer " + apiKey);
        }
        if (productHeader != null && !productHeader.isBlank()) {
            request.header("product", productHeader);
        }
        try {
            String response = request.retrieve().body(String.class);
            JsonNode root = mapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.isNull() || content.asText().isBlank()) {
                throw new IllegalStateException("LLM response did not contain choices[0].message.content");
            }
            return content.asText();
        } catch (Exception ex) {
            throw new IllegalStateException("LLM request failed: " + ex.getMessage(), ex);
        }
    }
}
