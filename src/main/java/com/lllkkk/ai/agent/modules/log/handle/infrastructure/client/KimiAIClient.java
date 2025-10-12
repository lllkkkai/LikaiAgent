package com.lllkkk.ai.agent.modules.log.handle.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lllkkk.ai.agent.modules.log.handle.infrastructure.config.AIConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KimiAIClient {

    private final AIConfig aiConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public String analyzeLog(String prompt) {
        try {
            String url = aiConfig.getBaseUrl() + "/chat/completions";

            ChatRequest request = new ChatRequest();
            request.setModel(aiConfig.getModel());
            request.setMaxTokens(aiConfig.getMaxTokens());
            request.setTemperature(aiConfig.getTemperature());

            ChatRequest.Message message = new ChatRequest.Message();
            message.setRole("user");
            message.setContent(prompt);
            request.setMessages(List.of(message));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiConfig.getApiKey());

            HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, ChatResponse.class);

            if (response.getBody() != null && !response.getBody().getChoices().isEmpty()) {
                return response.getBody().getChoices().get(0).getMessage().getContent();
            }

            return "AI分析失败：无法获取有效响应";

        } catch (Exception e) {
            log.error("调用Kimi AI API失败", e);
            return "AI分析失败：" + e.getMessage();
        }
    }

    @Data
    public static class ChatRequest {
        private String model;
        private List<Message> messages;
        private int maxTokens;
        private double temperature;

        @Data
        public static class Message {
            private String role;
            private String content;
        }
    }

    @Data
    public static class ChatResponse {
        private List<Choice> choices;

        @Data
        public static class Choice {
            private Message message;

            @Data
            public static class Message {
                private String content;
            }
        }
    }
}