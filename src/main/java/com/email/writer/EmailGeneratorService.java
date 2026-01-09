package com.email.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EmailGeneratorService {
    private final WebClient webClient;
    private final String apiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder, @Value("${gemini.api.url}") String baseUrl,@Value("${gemini.api.key}") String geminiApiKey) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = geminiApiKey;
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        //Build prompt
        String prompt=buildPrompt(emailRequest);
        //Prepare raw JSON Body
        String requestBody=String.format("""
                {
                    "contents": [
                      {
                        "parts": [
                          {
                            "text": "%s"
                          }
                        ]
                      }
                    ]
                  }""",prompt);
        //Send Request
        String response=webClient.post() //create Post request
                .uri(uriBuilder -> uriBuilder  //sets the endpoint url for the request
                        .path("/v1beta/models/gemini-2.5-flash:generateContent")
                        .build())
                .header("x-goog-api-key", apiKey)   ////adds api key to request header
                .header("Content-Type","application/json") //tells the server that you are sending json data in the body of the request
                .bodyValue(requestBody)     //adds request data to the post request
                .retrieve()
                .bodyToMono(String.class)
                .block();
        //Extract Response
        return extractResponseContent(response);


    }

    private String extractResponseContent(String response) {

        try {
            ObjectMapper mapper=new ObjectMapper();
            JsonNode root=mapper.readTree(response);
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt=new StringBuilder();
        prompt.append("Generate a professional email reply for the following  email:");
        if(emailRequest.getTone()!=null && !emailRequest.getTone().isEmpty()){
            prompt.append("Use a ").append(emailRequest.getTone()).append("tone.");
            //Use a casual tone.
        }
        prompt.append("Original Email: \n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}
