package com.example._Do.task.service;

import com.example._Do.task.dto.AiTaskResponse;
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

    // TODO refactor
    public AiTaskResponse processVoiceTask(MultipartFile file) {
        try {
            log.info("Processing voice file: {}, size: {}", file.getOriginalFilename(), file.getSize());
            String base64Audio = Base64.getEncoder().encodeToString(file.getBytes());
            String today = java.time.LocalDate.now().toString();
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("Api-Key is not found.");
            }

            String englishPrompt = """
            You are a task management assistant. Analyze this voice recording:
            1. Language: Identify the language spoken in the recording. Return 'title' and 'description' in that SAME language.
            2. Date Control:
               - Use the Reference Date (%s) to calculate relative dates like 'tomorrow', 'next Friday', etc.
               - If a specific date or time is mentioned, return it in 'YYYY-MM-DDTHH:mm:ss' format.
               - If NO date or time is mentioned, set 'dueDate' to null.
            3. If a clear task or 'to-do' is identified:
               - Set 'isTaskDetected' to true.
               - Fill 'title', 'description', 'priority', and 'dueDate'.
            4. If the audio is empty, contains only noise, or no task is mentioned:
               - Set 'isTaskDetected' to false.
               - Leave other fields null or empty.
            
            Return ONLY a valid JSON object in this format:
            {
              "title": "Short task title",
              "description": "Detailed description",
              "priority": "LOW/MEDIUM/HIGH",
              "dueDate": "YYYY-MM-DDTHH:mm:ss",
              "isTaskDetected": true/false
            }
            """.formatted(today);

            Map<String, Object> textPart = Map.of(
                    "text", englishPrompt
            );

            Map<String, Object> audioPart = Map.of(
                    "inline_data", Map.of(
                            "mime_type", "audio/wav",
                            "data", base64Audio
                    )
            );

            Map<String, Object> contentObject = Map.of("parts", List.of(textPart, audioPart));
            Map<String, Object> requestBody = Map.of("contents", List.of(contentObject));

            RestClient restClient = RestClient.create();
            String response = restClient.post()
                    .uri(geminiUrl + "?key=" + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("Response: {}", response);
            return parseGeminiResponse(response);

        } catch (Exception e) {
            log.error("AI error: ", e);
            // TODO add custom error
            throw new RuntimeException("Error:" , e);
        }
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
