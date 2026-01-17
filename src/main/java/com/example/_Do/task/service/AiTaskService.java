package com.example._Do.task.service;

import com.example._Do.task.dto.AiTaskResponse;
import com.example._Do.task.prompt.GeminiPromptBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskService {

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Value("${google.gemini.url}")
    private String geminiUrl;

    private final ObjectMapper objectMapper;
    private final GeminiPromptBuilder geminiPromptBuilder;
    private final RestClient restClient;

    // TODO refactor
    public AiTaskResponse processVoiceTask(MultipartFile file) {
        try {
            if (isApiKeyValid()) {
                throw new RuntimeException("Api-Key is not found.");
            }

            log.info("Processing voice file: {}, size: {}", file.getOriginalFilename(), file.getSize());

            String base64Audio = Base64.getEncoder().encodeToString(file.getBytes());
            String englishPrompt = geminiPromptBuilder.buildTaskDetectionPrompt();

            Map<String, Object> requestBody = buildRequestBody(englishPrompt, base64Audio);

            String response = geminiApiCall(requestBody);

            log.info("Response: {}", response);
            return parseGeminiResponse(response);

        } catch (Exception e) {
            log.error("AI error: ", e);
            // TODO add custom error
            throw new RuntimeException("Error:" , e);
        }
    }

    private boolean isApiKeyValid() {
        return apiKey != null && !apiKey.isEmpty();
    }

    private String geminiApiCall(Map<String, Object> requestBody) {
        return restClient.post()
                .uri(geminiUrl + "?key=" + apiKey)
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }

    private Map<String, Object> buildRequestBody(String prompt, String base64Audio){
        return Map.of("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", prompt),
                        Map.of("inline_data", Map.of(
                                "mime_type", "audio/wav",
                                "data", base64Audio
                        ))
                ))
        ));
    }

    private AiTaskResponse parseGeminiResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        String aiText = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        String cleanJson = aiText.replaceAll("```json", "").replaceAll("```", "").trim();

        return objectMapper.readValue(cleanJson, AiTaskResponse.class);
    }

}
