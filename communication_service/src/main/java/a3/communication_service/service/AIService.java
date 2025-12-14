package a3.communication_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AIService {

    @Value("${google.ai.key}")
    private String apiKey;
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";
    private final RestTemplate restTemplate;

    public AIService() {
        this.restTemplate = new RestTemplate();
    }

    public String getAIResponse(String userMessage) {
        try {
            String finalUrl = BASE_URL + "?key=" + apiKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = userMessage.replace("\"", "'").replace("\n", " ");
            String requestBody = String.format(
                    "{\"contents\": [{\"parts\": [{\"text\": \"Answer strictly in plain text, no markdown. Question: %s\"}]}]}",
                    prompt
            );
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(finalUrl, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();

            if (responseBody != null && responseBody.contains("\"text\":")) {
                int textIndex = responseBody.indexOf("\"text\":");
                if (textIndex != -1) {
                    int startQuote = responseBody.indexOf("\"", textIndex + 7);
                    int endQuote = responseBody.indexOf("\"", startQuote + 1);

                    while (endQuote != -1 && responseBody.charAt(endQuote - 1) == '\\') {
                        endQuote = responseBody.indexOf("\"", endQuote + 1);
                    }

                    if (startQuote != -1 && endQuote != -1) {
                        String rawAnswer = responseBody.substring(startQuote + 1, endQuote);
                        return rawAnswer.replace("\\n", "\n").replace("\\\"", "\"");
                    }
                }
            }

            return "Gemini connected but parsing failed. Raw: " + (responseBody != null ? responseBody.substring(0, Math.min(responseBody.length(), 100)) : "null");

        } catch (HttpClientErrorException e) {
            System.err.println("EROARE GEMINI (" + e.getStatusCode() + "): " + e.getResponseBodyAsString());
            return "AI Error: " + e.getStatusCode();
        } catch (Exception e) {
            e.printStackTrace();
            return "Internal Error: " + e.getMessage();
        }
    }
}