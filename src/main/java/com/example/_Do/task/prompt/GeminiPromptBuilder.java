package com.example._Do.task.prompt;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class GeminiPromptBuilder {
    public String buildTaskDetectionPrompt() {
        return String.join("\n\n",
                getPersona(),
                getContext(),
                getInstructions(),
                getOutputSchema()
        );
    }

    private String getPersona() {
        return "You are a professional task management assistant specialized in voice-to-task conversion.";
    }

    private String getContext() {
        String today = LocalDate.now().toString();
        return "Reference Date (Today): " + today + ". " +
                "Interpret relative terms like 'tomorrow', 'next week' or 'tonight' based on this date.";
    }

    private String getInstructions() {
        return """
            ### INSTRUCTIONS:
            1. LANGUAGE: Detect the language of the recording and provide 'title' and 'description' in that same language.
            2. EXTRACTION: Identify the core task. Extract priority and due date.
            3. DATE FORMAT: If a time/date is found, use 'YYYY-MM-DDTHH:mm:ss'. If not, use null.
            4. VALIDATION: Set 'isTaskDetected' to false if the audio is empty, noisy, or doesn't contain a clear command.
            """;
    }

    private String getOutputSchema() {
        return """
            ### OUTPUT FORMAT:
            Return ONLY a valid JSON object:
            {
              "title": "Short summary",
              "description": "Full details",
              "priority": "LOW/MEDIUM/HIGH",
              "dueDate": "ISO_DATE_TIME or null",
              "isTaskDetected": boolean
            }
            """;
    }
}
